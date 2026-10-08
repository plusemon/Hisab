package com.plusemon.hisab.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.plusemon.hisab.data.local.AppDatabase
import com.plusemon.hisab.data.model.AccountType
import com.plusemon.hisab.data.model.Budget
import com.plusemon.hisab.data.model.Category
import com.plusemon.hisab.data.model.Contact
import com.plusemon.hisab.data.model.DebtType
import com.plusemon.hisab.data.model.Frequency
import com.plusemon.hisab.data.model.LoanDebt
import com.plusemon.hisab.data.model.LoanRepayment
import com.plusemon.hisab.data.model.RecurringRule
import com.plusemon.hisab.data.model.SavingsGoal
import com.plusemon.hisab.data.model.ShopCreditPayment
import com.plusemon.hisab.data.model.ShopCreditPurchase
import com.plusemon.hisab.data.model.TransactionRecord
import com.plusemon.hisab.data.model.TransactionType
import com.plusemon.hisab.data.model.TransactionWithDetails
import com.plusemon.hisab.data.model.User
import com.plusemon.hisab.data.model.UserAccount
import com.plusemon.hisab.data.model.UserSettings
import com.plusemon.hisab.data.model.Vendor
import com.plusemon.hisab.data.model.UpdateInfo
import com.plusemon.hisab.data.repository.AuthRepository
import com.plusemon.hisab.data.repository.AuthResult
import com.plusemon.hisab.data.repository.FirestoreRepository
import com.plusemon.hisab.data.repository.FirestoreRepositoryImpl
import com.plusemon.hisab.data.repository.HisabRepository
import com.plusemon.hisab.data.repository.UpdateManager
import com.plusemon.hisab.data.repository.UpdateRepository
import com.plusemon.hisab.domain.util.CsvExporterImporter
import com.plusemon.hisab.domain.util.Formatters
import com.plusemon.hisab.domain.util.NaturalLanguageParser
import com.plusemon.hisab.domain.util.ParsedQuickEntry
import com.plusemon.hisab.domain.util.VersionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class UpdateUiState {
    object Idle : UpdateUiState()
    object Checking : UpdateUiState()
    data class Available(val updateInfo: UpdateInfo, val isManual: Boolean) : UpdateUiState()
    data class UpToDate(val currentVersion: String) : UpdateUiState()
    data class Downloading(val updateInfo: UpdateInfo, val progress: Float) : UpdateUiState()
    data class Downloaded(val updateInfo: UpdateInfo) : UpdateUiState()
    data class PermissionNeeded(val updateInfo: UpdateInfo) : UpdateUiState()
}

data class CategorySpendProgress(
    val category: Category,
    val spentAmount: Double,
    val budgetLimit: Double?,
    val percentage: Float, // e.g. 0.75f = 75%
    val status: BudgetStatus // SAFE (<70%), WARNING (70-100%), EXCEEDED (>100%), NO_BUDGET
)

enum class BudgetStatus {
    SAFE, WARNING, EXCEEDED, NO_BUDGET
}

data class AccountWithBalance(
    val account: UserAccount,
    val balance: Double
)

data class PeriodReport(
    val totalIncome: Double,
    val totalExpense: Double,
    val netSavings: Double,
    val savingsRate: Float,
    val categoryBreakdown: List<Pair<Category, Double>>,
    val dailyBreakdown: List<Pair<String, Double>>,
    val transactionCount: Int
)

class HisabViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val authRepository = AuthRepository(application, db)
    val hisabRepository = HisabRepository(db)
    val firestoreRepository: FirestoreRepository = FirestoreRepositoryImpl(application, db)
    val updateRepository = UpdateRepository()
    val updateManager = UpdateManager(application)

    private val _updateUiState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val updateUiState = _updateUiState.asStateFlow()

    val currentUser: StateFlow<User?> = authRepository.currentUser

    private val _isAuthInitializing = MutableStateFlow(true)
    val isAuthInitializing = _isAuthInitializing.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading = _isAuthLoading.asStateFlow()

    private val appPrefs = application.getSharedPreferences("hisab_settings_prefs", android.content.Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(
        UserSettings(
            userId = "",
            language = appPrefs.getString("language", "bn") ?: "bn",
            numeralSystem = appPrefs.getString("numeral_system", "bn") ?: "bn"
        )
    )
    val settings = _settings.asStateFlow()

    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked = _isAppLocked.asStateFlow()

    private val _accountsWithBalances = MutableStateFlow<List<AccountWithBalance>>(emptyList())
    val accountsWithBalances = _accountsWithBalances.asStateFlow()

    private val _archivedAccountsWithBalances = MutableStateFlow<List<AccountWithBalance>>(emptyList())
    val archivedAccountsWithBalances = _archivedAccountsWithBalances.asStateFlow()

    private val _totalBalance = MutableStateFlow(0.0)
    val totalBalance = _totalBalance.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionWithDetails>>(emptyList())
    val transactions = _transactions.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets = _budgets.asStateFlow()

    private val _categoryProgressList = MutableStateFlow<List<CategorySpendProgress>>(emptyList())
    val categoryProgressList = _categoryProgressList.asStateFlow()

    private val _overallBudgetProgress = MutableStateFlow<CategorySpendProgress?>(null)
    val overallBudgetProgress = _overallBudgetProgress.asStateFlow()

    private val _savingsGoals = MutableStateFlow<List<SavingsGoal>>(emptyList())
    val savingsGoals = _savingsGoals.asStateFlow()

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts = _contacts.asStateFlow()

    private val _loansDebts = MutableStateFlow<List<LoanDebt>>(emptyList())
    val loansDebts = _loansDebts.asStateFlow()

    private val _loanRepayments = MutableStateFlow<List<LoanRepayment>>(emptyList())
    val loanRepayments = _loanRepayments.asStateFlow()

    private val _vendors = MutableStateFlow<List<Vendor>>(emptyList())
    val vendors = _vendors.asStateFlow()

    private val _shopCreditPurchases = MutableStateFlow<List<ShopCreditPurchase>>(emptyList())
    val shopCreditPurchases = _shopCreditPurchases.asStateFlow()

    private val _shopCreditPayments = MutableStateFlow<List<ShopCreditPayment>>(emptyList())
    val shopCreditPayments = _shopCreditPayments.asStateFlow()

    private val _recurringRules = MutableStateFlow<List<RecurringRule>>(emptyList())
    val recurringRules = _recurringRules.asStateFlow()

    private val _insights = MutableStateFlow<List<String>>(emptyList())
    val insights = _insights.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    private var deletedTransactionUndo: TransactionRecord? = null

    private var activeDataJobs = mutableListOf<Job>()

    init {
        viewModelScope.launch {
            authRepository.loadInitialUser()
            _isAuthInitializing.value = false
            authRepository.currentUser.collect { user ->
                cancelUserJobs()
                if (user != null) {
                    authRepository.ensureUserDataSeeded(user.id)
                    observeUserData(user.id)
                    // Auto-process recurring rules on login/open
                    hisabRepository.processRecurringTransactions(user.id)
                    // Background sync with Firestore to prevent data loss across devices/restarts
                    syncWithFirestore(user.id)
                } else {
                    resetUserData()
                }
            }
        }
        // Check for app updates silently on startup
        checkForUpdates(isManual = false)
    }

    private fun syncWithFirestore(userId: String) {
        viewModelScope.launch {
            try {
                firestoreRepository.syncAllWithRoom(userId)
            } catch (e: Exception) {
                android.util.Log.w("HisabViewModel", "Background sync with Firestore error: ${e.message}")
            }
        }
    }

    fun triggerManualSync() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            _snackbarMessage.emit(if (_settings.value.language == "bn") "ক্লাউড সিঙ্ক শুরু হচ্ছে..." else "Starting cloud sync...")
            val result = firestoreRepository.syncAllWithRoom(user.id)
            if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                _snackbarMessage.emit(
                    if (_settings.value.language == "bn")
                        "ক্লাউড সিঙ্ক সফল হয়েছে ($count টি ডাটা সিঙ্ক হয়েছে)"
                    else
                        "Cloud sync complete ($count records synced)"
                )
            } else {
                _snackbarMessage.emit(
                    if (_settings.value.language == "bn")
                        "সিঙ্ক ব্যর্থ হয়েছে: ${result.exceptionOrNull()?.localizedMessage ?: "নেটওয়ার্ক সমস্যা"}"
                    else
                        "Sync failed: ${result.exceptionOrNull()?.localizedMessage ?: "Network error"}"
                )
            }
        }
    }

    fun ensureDefaultData() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            authRepository.ensureUserDataSeeded(user.id)
        }
    }

    private fun cancelUserJobs() {
        activeDataJobs.forEach { it.cancel() }
        activeDataJobs.clear()
    }

    private fun resetUserData() {
        val savedLang = appPrefs.getString("language", "bn") ?: "bn"
        val savedNumeral = appPrefs.getString("numeral_system", "bn") ?: "bn"
        _settings.value = UserSettings(userId = "", language = savedLang, numeralSystem = savedNumeral)
        _isAppLocked.value = false
        _accountsWithBalances.value = emptyList()
        _archivedAccountsWithBalances.value = emptyList()
        _totalBalance.value = 0.0
        _transactions.value = emptyList()
        _categories.value = emptyList()
        _budgets.value = emptyList()
        _savingsGoals.value = emptyList()
        _contacts.value = emptyList()
        _loansDebts.value = emptyList()
        _loanRepayments.value = emptyList()
        _vendors.value = emptyList()
        _shopCreditPurchases.value = emptyList()
        _shopCreditPayments.value = emptyList()
        _recurringRules.value = emptyList()
        _insights.value = emptyList()
    }

    private fun observeUserData(userId: String) {
        val currentMonthYear = Formatters.getCurrentMonthYear()

        // 1. Settings Flow
        val settingsJob = viewModelScope.launch {
            hisabRepository.getUserSettings(userId).collect { userSettings ->
                if (userSettings != null) {
                    val savedLang = appPrefs.getString("language", userSettings.language) ?: userSettings.language
                    val savedNumeral = appPrefs.getString("numeral_system", userSettings.numeralSystem) ?: userSettings.numeralSystem
                    val activeSettings = if (userSettings.language != savedLang || userSettings.numeralSystem != savedNumeral) {
                        val synced = userSettings.copy(language = savedLang, numeralSystem = savedNumeral)
                        hisabRepository.updateSettings(synced)
                        synced
                    } else {
                        userSettings
                    }
                    _settings.value = activeSettings
                    if (activeSettings.pinEnabled && activeSettings.pinCode.isNotBlank()) {
                        _isAppLocked.value = true
                    }
                }
            }
        }
        activeDataJobs.add(settingsJob)

        // 2. Accounts & Live Balances (Unified Single Source of Truth)
        val accountsJob = viewModelScope.launch {
            combine(
                hisabRepository.getAllAccounts(userId),
                hisabRepository.calculateAccountBalances(userId)
            ) { accounts, balanceMap ->
                val activeAccounts = accounts.filter { !it.isArchived }
                val archivedAccounts = accounts.filter { it.isArchived }
                val mappedActive = activeAccounts.map { acc ->
                    AccountWithBalance(
                        account = acc,
                        balance = balanceMap[acc.id] ?: acc.startingBalance
                    )
                }
                val mappedArchived = archivedAccounts.map { acc ->
                    AccountWithBalance(
                        account = acc,
                        balance = balanceMap[acc.id] ?: acc.startingBalance
                    )
                }
                val total = mappedActive.sumOf { it.balance }
                Triple(mappedActive, mappedArchived, total)
            }.collect { (activeList, archivedList, total) ->
                _accountsWithBalances.value = activeList
                _archivedAccountsWithBalances.value = archivedList
                _totalBalance.value = total
            }
        }
        activeDataJobs.add(accountsJob)

        // 3. Transactions Flow
        val txJob = viewModelScope.launch {
            hisabRepository.getTransactionsWithDetails(userId).collect { txList ->
                _transactions.value = txList
                calculateBudgetsAndInsights(userId, txList, currentMonthYear)
            }
        }
        activeDataJobs.add(txJob)

        // 4. Categories Flow
        val catJob = viewModelScope.launch {
            hisabRepository.getActiveCategories(userId).collect { catList ->
                _categories.value = catList
            }
        }
        activeDataJobs.add(catJob)

        // 5. Budgets Flow
        val budgetJob = viewModelScope.launch {
            hisabRepository.getBudgetsForMonth(userId, currentMonthYear).collect { bList ->
                _budgets.value = bList
                calculateBudgetsAndInsights(userId, _transactions.value, currentMonthYear)
            }
        }
        activeDataJobs.add(budgetJob)

        // 6. Savings Goals Flow
        val goalsJob = viewModelScope.launch {
            hisabRepository.getSavingsGoals(userId).collect { gList ->
                _savingsGoals.value = gList
            }
        }
        activeDataJobs.add(goalsJob)

        // 7. Loans & Debts Flow
        val debtsJob = viewModelScope.launch {
            hisabRepository.getLoansAndDebts(userId).collect { dList ->
                _loansDebts.value = dList
            }
        }
        activeDataJobs.add(debtsJob)

        // 7b. Contacts Flow
        val contactsJob = viewModelScope.launch {
            hisabRepository.getContacts(userId).collect { cList ->
                _contacts.value = cList
            }
        }
        activeDataJobs.add(contactsJob)

        // 7c. Loan Repayments Flow
        val repaymentsJob = viewModelScope.launch {
            hisabRepository.getLoanRepayments(userId).collect { rList ->
                _loanRepayments.value = rList
            }
        }
        activeDataJobs.add(repaymentsJob)

        // 7d. Vendors Flow
        val vendorsJob = viewModelScope.launch {
            hisabRepository.getVendors(userId).collect { vList ->
                _vendors.value = vList
            }
        }
        activeDataJobs.add(vendorsJob)

        // 7e. Shop Credit Purchases Flow
        val creditPurchasesJob = viewModelScope.launch {
            hisabRepository.getShopCreditPurchases(userId).collect { pList ->
                _shopCreditPurchases.value = pList
            }
        }
        activeDataJobs.add(creditPurchasesJob)

        // 7f. Shop Credit Payments Flow
        val creditPaymentsJob = viewModelScope.launch {
            hisabRepository.getShopCreditPayments(userId).collect { payList ->
                _shopCreditPayments.value = payList
            }
        }
        activeDataJobs.add(creditPaymentsJob)

        // 8. Recurring Rules Flow
        val recurringJob = viewModelScope.launch {
            hisabRepository.getRecurringRules(userId).collect { rList ->
                _recurringRules.value = rList
            }
        }
        activeDataJobs.add(recurringJob)
    }

    private fun calculateBudgetsAndInsights(
        userId: String,
        txList: List<TransactionWithDetails>,
        monthYear: String
    ) {
        val (monthStart, monthEnd) = Formatters.getStartAndEndOfMonth(monthYear)
        val monthExpenses = txList.filter {
            it.transaction.type == TransactionType.EXPENSE &&
                    it.transaction.dateTimestamp in monthStart..monthEnd
        }

        val spentByCategory = monthExpenses
            .filter { it.transaction.categoryId != null }
            .groupBy { it.transaction.categoryId!! }
            .mapValues { entry -> entry.value.sumOf { it.transaction.amount } }

        val totalMonthExpense = monthExpenses.sumOf { it.transaction.amount }
        val budgetMap = _budgets.value.associateBy { it.categoryId }

        val progressList = _categories.value.filter { it.type == TransactionType.EXPENSE }.map { cat ->
            val spent = spentByCategory[cat.id] ?: 0.0
            val budget = budgetMap[cat.id]
            val limit = budget?.limitAmount
            val percentage = if (limit != null && limit > 0) (spent / limit).toFloat() else 0f
            val status = when {
                limit == null -> BudgetStatus.NO_BUDGET
                spent > limit -> BudgetStatus.EXCEEDED
                spent >= limit * 0.70 -> BudgetStatus.WARNING
                else -> BudgetStatus.SAFE
            }
            CategorySpendProgress(
                category = cat,
                spentAmount = spent,
                budgetLimit = limit,
                percentage = percentage,
                status = status
            )
        }
        _categoryProgressList.value = progressList

        val overallBudget = budgetMap[null]
        if (overallBudget != null) {
            val limit = overallBudget.limitAmount
            val percentage = if (limit > 0) (totalMonthExpense / limit).toFloat() else 0f
            val status = when {
                totalMonthExpense > limit -> BudgetStatus.EXCEEDED
                totalMonthExpense >= limit * 0.70 -> BudgetStatus.WARNING
                else -> BudgetStatus.SAFE
            }
            _overallBudgetProgress.value = CategorySpendProgress(
                category = Category(userId = userId, nameBn = "মোট বাজেট", nameEn = "Overall Budget", type = TransactionType.EXPENSE, iconName = "pie_chart", colorHex = "#0F766E"),
                spentAmount = totalMonthExpense,
                budgetLimit = limit,
                percentage = percentage,
                status = status
            )
        } else {
            _overallBudgetProgress.value = null
        }

        // Generate dynamic insights
        generateSmartInsights(txList, spentByCategory, totalMonthExpense)
    }

    private fun generateSmartInsights(
        txList: List<TransactionWithDetails>,
        currentMonthSpentByCat: Map<Long, Double>,
        currentMonthTotalExpense: Double
    ) {
        val isBn = _settings.value.language == "bn"
        val currSymbol = _settings.value.currencySymbol
        val insightList = mutableListOf<String>()

        // Find top expense category
        val topCategoryEntry = currentMonthSpentByCat.maxByOrNull { it.value }
        if (topCategoryEntry != null && topCategoryEntry.value > 0) {
            val topCat = _categories.value.firstOrNull { it.id == topCategoryEntry.key }
            if (topCat != null) {
                val catName = topCat.localizedName(isBn)
                val formattedAmount = Formatters.formatAmount(topCategoryEntry.value, currSymbol, _settings.value.numeralSystem == "bn")
                if (isBn) {
                    insightList.add("চলতি মাসে আপনার সর্বোচ্চ খরচ হয়েছে '$catName' খাতে ($formattedAmount)।")
                } else {
                    insightList.add("Your highest expense this month was on '$catName' ($formattedAmount).")
                }
            }
        }

        // Compare with last month
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.MONTH, -1)
        val lastMonthStr = String.format(java.util.Locale.US, "%04d-%02d", cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1)
        val (lastStart, lastEnd) = Formatters.getStartAndEndOfMonth(lastMonthStr)
        val lastMonthExpenses = txList.filter {
            it.transaction.type == TransactionType.EXPENSE &&
                    it.transaction.dateTimestamp in lastStart..lastEnd
        }.sumOf { it.transaction.amount }

        if (lastMonthExpenses > 0 && currentMonthTotalExpense > 0) {
            val diffPercent = ((currentMonthTotalExpense - lastMonthExpenses) / lastMonthExpenses * 100).toInt()
            if (diffPercent < 0) {
                val absDiff = Math.abs(diffPercent)
                if (isBn) {
                    insightList.add("অভিনন্দন! গত মাসের তুলনায় আপনার খরচ ${Formatters.toBanglaDigits(absDiff.toString())}% কমেছে।")
                } else {
                    insightList.add("Great job! You spent $absDiff% less compared to last month.")
                }
            } else if (diffPercent > 10) {
                if (isBn) {
                    insightList.add("সতর্কতা: গত মাসের তুলনায় খরচ ${Formatters.toBanglaDigits(diffPercent.toString())}% বৃদ্ধি পেয়েছে।")
                } else {
                    insightList.add("Notice: Expenses are $diffPercent% higher than last month.")
                }
            }
        }

        // Check budget warning
        val exceededCount = _categoryProgressList.value.count { it.status == BudgetStatus.EXCEEDED }
        if (exceededCount > 0) {
            if (isBn) {
                insightList.add("সতর্কতা: ${Formatters.toBanglaDigits(exceededCount.toString())}টি ক্যাটাগরির বাজেট সীমা পার হয়েছে!")
            } else {
                insightList.add("Warning: $exceededCount categories have exceeded their budget limits!")
            }
        }

        _insights.value = insightList
    }

    // -------------------------------------------------------------
    // AUTH ACTIONS
    // -------------------------------------------------------------

    fun signInWithGoogle(uid: String? = null, email: String, name: String, photoUrl: String? = null) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            when (val result = authRepository.signInWithGoogle(uid, email, name, photoUrl)) {
                is AuthResult.Success -> {
                    _isAuthLoading.value = false
                }
                is AuthResult.Error -> {
                    _isAuthLoading.value = false
                    _authError.value = result.message
                }
            }
        }
    }

    fun handleGoogleSignInFailure(e: Throwable) {
        _isAuthLoading.value = false
        val apiException = findApiException(e)
        val statusCode = apiException?.statusCode ?: extractStatusCodeFromException(e)

        val isNoCredential = e is androidx.credentials.exceptions.NoCredentialException ||
                e.message?.contains("No credentials available", ignoreCase = true) == true ||
                e.cause?.message?.contains("No credentials available", ignoreCase = true) == true

        val isCancellation = e is androidx.credentials.exceptions.GetCredentialCancellationException ||
                statusCode == 16 || statusCode == 12501 ||
                e.message?.contains("cancel", ignoreCase = true) == true

        if (isCancellation || isNoCredential) {
            android.util.Log.i(
                "GoogleSignIn",
                if (isCancellation) "Google Sign-In was cancelled by the user."
                else "No Google account found on device. Guiding user to email login or account settings."
            )
        } else {
            android.util.Log.w(
                "GoogleSignIn",
                "Google Sign-In failed! Status code: $statusCode, Message: ${apiException?.message ?: e.message}"
            )
        }

        if (isCancellation) {
            _authError.value = null
            return
        }

        val isBn = _settings.value.language == "bn"
        val errorMessage = when {
            // No credentials on device (e.g. fresh emulator or device without Google accounts)
            isNoCredential -> {
                if (isBn) {
                    "এই ডিভাইসে কোনো গুগল অ্যাকাউন্ট যোগ করা নেই। অনুগ্রহ করে নিচে ইমেইল ও পাসওয়ার্ড দিয়ে প্রবেশ/নিবন্ধন করুন অথবা ডিভাইসের সেটিংসে গুগল অ্যাকাউন্ট যোগ করুন।"
                } else {
                    "No Google account found on this device. Please sign in or create an account with Email & Password below, or add a Google account in device Settings."
                }
            }
            // DEVELOPER_ERROR / Configuration issue (Status code 10 or SHA-1 / package name mismatch)
            statusCode == 10 ||
                    e is androidx.credentials.exceptions.GetCredentialProviderConfigurationException ||
                    e.message?.contains("DEVELOPER_ERROR", ignoreCase = true) == true ||
                    e.message?.contains("10:", ignoreCase = true) == true ||
                    e.cause?.message?.contains("DEVELOPER_ERROR", ignoreCase = true) == true -> {
                if (isBn) {
                    "কনফিগারেশন সমস্যা (DEVELOPER_ERROR - Status Code 10)। গুগল ক্লায়েন্ট আইডি বা ফিঙ্গারপ্রিন্ট যাচাই করুন।"
                } else {
                    "Configuration issue (DEVELOPER_ERROR - Status Code 10). Please contact support to verify SHA-1 fingerprint registration and client ID configuration."
                }
            }
            // Network error (Status code 7)
            statusCode == 7 ||
                    e.message?.contains("NETWORK_ERROR", ignoreCase = true) == true ||
                    e.message?.contains("network", ignoreCase = true) == true ||
                    e is java.net.UnknownHostException || e is java.io.IOException -> {
                if (isBn) {
                    "ইন্টারনেট সংযোগে সমস্যা হয়েছে। অনুগ্রহ করে ইন্টারনেট সংযোগ পরীক্ষা করে পুনরায় চেষ্টা করুন।"
                } else {
                    "Network error during Google Sign-In. Please check your internet connection and try again."
                }
            }
            else -> {
                val codeString = if (statusCode != null) " (Status Code $statusCode)" else ""
                if (isBn) {
                    "গুগল সাইন-ইন সম্পন্ন করা যায়নি$codeString: ${apiException?.message ?: e.localizedMessage ?: "অজানা ত্রুটি"}"
                } else {
                    "Google Sign-In failed$codeString: ${apiException?.message ?: e.localizedMessage ?: "Unknown error"}"
                }
            }
        }

        _authError.value = errorMessage
    }

    private fun findApiException(e: Throwable?): com.google.android.gms.common.api.ApiException? {
        var current = e
        while (current != null) {
            if (current is com.google.android.gms.common.api.ApiException) {
                return current
            }
            current = current.cause
        }
        return null
    }

    private fun extractStatusCodeFromException(e: Throwable?): Int? {
        var current = e
        while (current != null) {
            val msg = current.message ?: ""
            val regex = Regex("""\b(status\s*code|status):\s*(\d+)""", RegexOption.IGNORE_CASE)
            val match = regex.find(msg)
            if (match != null) {
                return match.groupValues[2].toIntOrNull()
            }
            current = current.cause
        }
        return null
    }

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            when (val result = authRepository.signInWithEmail(email, pass)) {
                is AuthResult.Success -> {
                    _isAuthLoading.value = false
                }
                is AuthResult.Error -> {
                    _isAuthLoading.value = false
                    _authError.value = result.message
                }
            }
        }
    }

    fun signUpWithEmail(name: String, email: String, pass: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            when (val result = authRepository.signUpWithEmail(name, email, pass)) {
                is AuthResult.Success -> {
                    _isAuthLoading.value = false
                }
                is AuthResult.Error -> {
                    _isAuthLoading.value = false
                    _authError.value = result.message
                }
            }
        }
    }

    fun clearAuthError() {
        _authError.value = null
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun deleteAccount() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            authRepository.deleteAccountAndData(user.id)
        }
    }

    // -------------------------------------------------------------
    // QUICK NATURAL LANGUAGE ENTRY
    // -------------------------------------------------------------

    fun parseQuickEntry(text: String): ParsedQuickEntry? {
        val rawAccounts = _accountsWithBalances.value.map { it.account }
        return NaturalLanguageParser.parse(text, rawAccounts, _categories.value)
    }

    fun addParsedTransaction(parsed: ParsedQuickEntry) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            try {
                var accId = parsed.matchedAccount?.id ?: _accountsWithBalances.value.firstOrNull()?.account?.id ?: 0L
                if (accId <= 0L) {
                    val fallback = _accountsWithBalances.value.firstOrNull()?.account ?: db.accountDao().getFirstActiveAccount(user.id)
                    if (fallback != null) {
                        accId = fallback.id
                    } else {
                        val newAcc = UserAccount(
                            userId = user.id,
                            name = "Cash",
                            type = AccountType.CASH,
                            currencyCode = "BDT",
                            startingBalance = 0.0,
                            colorHex = "#0F766E",
                            iconName = "payments"
                        )
                        accId = hisabRepository.addAccount(newAcc)
                    }
                }
                val record = TransactionRecord(
                    userId = user.id,
                    accountId = accId,
                    categoryId = if (parsed.type != TransactionType.TRANSFER) parsed.matchedCategory?.id else null,
                    toAccountId = if (parsed.type == TransactionType.TRANSFER) parsed.matchedToAccount?.id else null,
                    amount = parsed.amount,
                    type = parsed.type,
                    dateTimestamp = System.currentTimeMillis(),
                    note = parsed.note
                )
                val newId = hisabRepository.insertTransaction(record)
                val savedRecord = record.copy(id = newId)
                viewModelScope.launch {
                    try {
                        firestoreRepository.saveTransaction(user.id, savedRecord)
                    } catch (e: Exception) {
                        android.util.Log.w("HisabViewModel", "Firestore async save error: ${e.message}")
                    }
                }
                _snackbarMessage.emit(if (_settings.value.language == "bn") "হিসাব সফলভাবে যুক্ত হয়েছে" else "Entry saved successfully")
            } catch (e: Exception) {
                android.util.Log.e("HisabViewModel", "Failed to add parsed transaction: ${e.message}", e)
                _snackbarMessage.emit(if (_settings.value.language == "bn") "হিসাব সংরক্ষণে ত্রুটি: ${e.localizedMessage}" else "Failed to save entry: ${e.localizedMessage}")
            }
        }
    }

    // -------------------------------------------------------------
    // TRANSACTIONS ACTIONS
    // -------------------------------------------------------------

    fun addTransaction(
        accountId: Long,
        categoryId: Long?,
        toAccountId: Long?,
        amount: Double,
        fee: Double,
        type: TransactionType,
        dateTimestamp: Long,
        note: String,
        receiptUri: String?,
        exchangeRate: Double = 1.0
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            try {
                var validAccountId = accountId
                if (validAccountId <= 0L) {
                    val fallbackAcc = _accountsWithBalances.value.firstOrNull()?.account ?: db.accountDao().getFirstActiveAccount(user.id)
                    if (fallbackAcc != null) {
                        validAccountId = fallbackAcc.id
                    } else {
                        val newAcc = UserAccount(
                            userId = user.id,
                            name = "Cash",
                            type = AccountType.CASH,
                            currencyCode = "BDT",
                            startingBalance = 0.0,
                            colorHex = "#0F766E",
                            iconName = "payments"
                        )
                        validAccountId = hisabRepository.addAccount(newAcc)
                    }
                }

                val record = TransactionRecord(
                    userId = user.id,
                    accountId = validAccountId,
                    categoryId = if (type != TransactionType.TRANSFER) categoryId else null,
                    toAccountId = if (type == TransactionType.TRANSFER) toAccountId else null,
                    amount = amount,
                    fee = fee,
                    type = type,
                    dateTimestamp = dateTimestamp,
                    note = note,
                    receiptUri = receiptUri,
                    exchangeRate = exchangeRate
                )
                val newId = hisabRepository.insertTransaction(record)
                val savedRecord = record.copy(id = newId)
                viewModelScope.launch {
                    try {
                        firestoreRepository.saveTransaction(user.id, savedRecord)
                    } catch (e: Exception) {
                        android.util.Log.w("HisabViewModel", "Firestore async save error: ${e.message}")
                    }
                }
                _snackbarMessage.emit(if (_settings.value.language == "bn") "লেনদেন সংরক্ষণ করা হয়েছে" else "Transaction saved successfully")
            } catch (e: Exception) {
                android.util.Log.e("HisabViewModel", "Failed to insert transaction: ${e.message}", e)
                _snackbarMessage.emit(if (_settings.value.language == "bn") "লেনদেন সংরক্ষণে সমস্যা হয়েছে: ${e.localizedMessage}" else "Failed to save transaction: ${e.localizedMessage}")
            }
        }
    }

    fun updateTransaction(
        id: Long,
        accountId: Long,
        categoryId: Long?,
        toAccountId: Long?,
        amount: Double,
        fee: Double,
        type: TransactionType,
        dateTimestamp: Long,
        note: String,
        receiptUri: String?,
        exchangeRate: Double = 1.0
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            try {
                val record = TransactionRecord(
                    id = id,
                    userId = user.id,
                    accountId = accountId,
                    categoryId = if (type != TransactionType.TRANSFER) categoryId else null,
                    toAccountId = if (type == TransactionType.TRANSFER) toAccountId else null,
                    amount = amount,
                    fee = fee,
                    type = type,
                    dateTimestamp = dateTimestamp,
                    note = note,
                    receiptUri = receiptUri,
                    exchangeRate = exchangeRate
                )
                hisabRepository.updateTransaction(record)
                viewModelScope.launch {
                    try {
                        firestoreRepository.updateTransaction(user.id, record)
                    } catch (e: Exception) {
                        android.util.Log.w("HisabViewModel", "Firestore async update error: ${e.message}")
                    }
                }
                _snackbarMessage.emit(if (_settings.value.language == "bn") "লেনদেন আপডেট করা হয়েছে" else "Transaction updated successfully")
            } catch (e: Exception) {
                android.util.Log.e("HisabViewModel", "Failed to update transaction: ${e.message}", e)
                _snackbarMessage.emit(if (_settings.value.language == "bn") "আপডেটে সমস্যা হয়েছে: ${e.localizedMessage}" else "Failed to update: ${e.localizedMessage}")
            }
        }
    }

    fun deleteTransaction(tx: TransactionRecord) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            deletedTransactionUndo = tx
            hisabRepository.deleteTransaction(tx.id, user.id)
            viewModelScope.launch {
                try {
                    firestoreRepository.deleteTransaction(user.id, tx.id)
                } catch (e: Exception) {
                    android.util.Log.w("HisabViewModel", "Firestore async delete error: ${e.message}")
                }
            }
            _snackbarMessage.emit("লেনদেন মুছে ফেলা হয়েছে")
        }
    }

    fun undoDeleteTransaction() {
        val tx = deletedTransactionUndo ?: return
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val restoredId = hisabRepository.insertTransaction(tx.copy(id = 0))
            val restored = tx.copy(id = restoredId)
            deletedTransactionUndo = null
            viewModelScope.launch {
                try {
                    firestoreRepository.saveTransaction(user.id, restored)
                } catch (e: Exception) {
                    android.util.Log.w("HisabViewModel", "Firestore async undo save error: ${e.message}")
                }
            }
            _snackbarMessage.emit("লেনদেন পুনরুদ্ধার করা হয়েছে")
        }
    }

    // -------------------------------------------------------------
    // ACCOUNTS ACTIONS
    // -------------------------------------------------------------

    fun addAccount(
        name: String,
        type: AccountType,
        currencyCode: String,
        startingBalance: Double,
        colorHex: String,
        iconName: String
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val acc = UserAccount(
                userId = user.id,
                name = name,
                type = type,
                currencyCode = currencyCode,
                startingBalance = startingBalance,
                colorHex = colorHex,
                iconName = iconName
            )
            hisabRepository.addAccount(acc)
            _snackbarMessage.emit("অ্যাকাউন্ট যোগ করা হয়েছে")
        }
    }

    fun updateAccount(
        id: Long,
        name: String,
        type: AccountType,
        currencyCode: String,
        startingBalance: Double,
        colorHex: String,
        iconName: String
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val acc = UserAccount(
                id = id,
                userId = user.id,
                name = name,
                type = type,
                currencyCode = currencyCode,
                startingBalance = startingBalance,
                colorHex = colorHex,
                iconName = iconName
            )
            hisabRepository.updateAccount(acc)
            _snackbarMessage.emit("অ্যাকাউন্ট আপডেট করা হয়েছে")
        }
    }

    fun archiveAccount(accountId: Long, isArchived: Boolean) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            hisabRepository.archiveAccount(accountId, user.id, isArchived)
            val updatedAcc = hisabRepository.getAccountById(accountId, user.id)
            if (updatedAcc != null) {
                try {
                    firestoreRepository.saveAccount(user.id, updatedAcc)
                } catch (e: Exception) {
                    android.util.Log.w("HisabViewModel", "Firestore sync account error: ${e.message}")
                }
            }
            _snackbarMessage.emit(
                if (isArchived) {
                    if (_settings.value.language == "bn")
                        "অ্যাকাউন্ট আর্কাইভ করা হয়েছে (নিচের 'আর্কাইভ করা অ্যাকাউন্ট' সেকশনে দেখতে পারেন)"
                    else
                        "Account archived (available in Archived Accounts section below)"
                } else {
                    if (_settings.value.language == "bn")
                        "অ্যাকাউন্ট পুনরুদ্ধার করা হয়েছে"
                    else
                        "Account restored successfully"
                }
            )
        }
    }

    fun deleteAccount(accountId: Long) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val deleted = hisabRepository.deleteAccount(accountId, user.id)
            if (!deleted) {
                _snackbarMessage.emit("এই অ্যাকাউন্টে লেনদেনের হিসেব রয়েছে, তাই মুছা যাবে না। আর্কাইভ করতে পারেন।")
            } else {
                _snackbarMessage.emit("অ্যাকাউন্ট মুছে ফেলা হয়েছে")
            }
        }
    }

    // -------------------------------------------------------------
    // CATEGORIES ACTIONS
    // -------------------------------------------------------------

    fun addCategory(
        nameBn: String,
        nameEn: String,
        type: TransactionType,
        iconName: String,
        colorHex: String
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val cat = Category(
                userId = user.id,
                nameBn = nameBn,
                nameEn = nameEn,
                type = type,
                iconName = iconName,
                colorHex = colorHex
            )
            hisabRepository.addCategory(cat)
            _snackbarMessage.emit("ক্যাটাগরি যোগ করা হয়েছে")
        }
    }

    fun updateCategory(
        id: Long,
        nameBn: String,
        nameEn: String,
        type: TransactionType,
        iconName: String,
        colorHex: String
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val cat = Category(
                id = id,
                userId = user.id,
                nameBn = nameBn,
                nameEn = nameEn,
                type = type,
                iconName = iconName,
                colorHex = colorHex
            )
            hisabRepository.updateCategory(cat)
            _snackbarMessage.emit("ক্যাটাগরি আপডেট করা হয়েছে")
        }
    }

    fun deleteCategory(categoryId: Long) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val hardDeleted = hisabRepository.deleteCategory(categoryId, user.id)
            if (hardDeleted) {
                _snackbarMessage.emit("ক্যাটাগরি মুছে ফেলা হয়েছে")
            } else {
                _snackbarMessage.emit("ক্যাটাগরিটিতে লেনদেন থাকায় এটি আর্কাইভ করা হয়েছে")
            }
        }
    }

    // -------------------------------------------------------------
    // BUDGETS ACTIONS
    // -------------------------------------------------------------

    fun saveBudget(categoryId: Long?, limitAmount: Double, monthYear: String = Formatters.getCurrentMonthYear()) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val existing = _budgets.value.firstOrNull { it.categoryId == categoryId && it.monthYear == monthYear }
            val budget = Budget(
                id = existing?.id ?: 0L,
                userId = user.id,
                categoryId = categoryId,
                monthYear = monthYear,
                limitAmount = limitAmount
            )
            hisabRepository.setBudget(budget)
            _snackbarMessage.emit("বাজেট সংরক্ষণ করা হয়েছে")
        }
    }

    fun deleteBudget(budgetId: Long) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            hisabRepository.deleteBudget(budgetId, user.id)
            _snackbarMessage.emit("বাজেট মুছে ফেলা হয়েছে")
        }
    }

    // -------------------------------------------------------------
    // SAVINGS GOALS ACTIONS
    // -------------------------------------------------------------

    fun addSavingsGoal(name: String, targetAmount: Double, targetDate: Long?, note: String, colorHex: String, iconName: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val goal = SavingsGoal(
                userId = user.id,
                name = name,
                targetAmount = targetAmount,
                targetDate = targetDate,
                note = note,
                colorHex = colorHex,
                iconName = iconName
            )
            hisabRepository.addSavingsGoal(goal)
            _snackbarMessage.emit("সঞ্চয় লক্ষ্য যুক্ত করা হয়েছে")
        }
    }

    fun adjustGoalSavings(id: Long, deltaAmount: Double) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            hisabRepository.adjustGoalSavings(id, user.id, deltaAmount)
            _snackbarMessage.emit("সঞ্চয় আপডেট হয়েছে")
        }
    }

    fun deleteSavingsGoal(id: Long) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            hisabRepository.deleteSavingsGoal(id, user.id)
            _snackbarMessage.emit("সঞ্চয় লক্ষ্য মুছে ফেলা হয়েছে")
        }
    }

    // -------------------------------------------------------------
    // LOANS & DEBTS ACTIONS
    // -------------------------------------------------------------

    fun addContact(name: String, phone: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            hisabRepository.addContact(Contact(userId = user.id, name = name, phone = phone))
            _snackbarMessage.emit("কন্টাক্ট যুক্ত করা হয়েছে")
        }
    }

    fun updateContact(contact: Contact) {
        viewModelScope.launch {
            hisabRepository.updateContact(contact)
            _snackbarMessage.emit("কন্টাক্টের তথ্য আপডেট করা হয়েছে")
        }
    }

    fun addLoanDebt(
        contactId: Long,
        personName: String,
        accountId: Long,
        amount: Double,
        type: DebtType,
        dueDate: Long?,
        note: String,
        phone: String
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val debt = LoanDebt(
                userId = user.id,
                contactId = contactId,
                personName = personName,
                accountId = accountId,
                amount = amount,
                type = type,
                dueDate = dueDate,
                note = note,
                phone = phone
            )
            hisabRepository.addLoanDebt(debt)
            _snackbarMessage.emit("ধার/লেনদেন হিসাব সংরক্ষণ করা হয়েছে")
        }
    }

    fun recordDebtPayment(id: Long, accountId: Long, paymentAmount: Double, note: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            hisabRepository.recordLoanRepayment(id, user.id, accountId, paymentAmount, note)
            _snackbarMessage.emit("পরিশোধের তথ্য সংরক্ষণ করা হয়েছে")
        }
    }

    fun deleteLoanDebt(id: Long) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            hisabRepository.deleteLoanDebt(id, user.id)
            _snackbarMessage.emit("হিসাব মুছে ফেলা হয়েছে")
        }
    }

    // -------------------------------------------------------------
    // SHOP CREDIT (দোকান বাকি) ACTIONS
    // -------------------------------------------------------------

    fun addVendor(
        name: String,
        phone: String,
        locationNote: String,
        categoryTag: String = "",
        onCreated: ((Vendor) -> Unit)? = null
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val vendor = Vendor(
                userId = user.id,
                name = name,
                phone = phone,
                locationNote = locationNote,
                categoryTag = categoryTag
            )
            val newId = hisabRepository.addVendor(vendor)
            val created = vendor.copy(id = newId)
            onCreated?.invoke(created)
            _snackbarMessage.emit(if (_settings.value.language == "bn") "দোকান যুক্ত করা হয়েছে" else "Shop added successfully")
        }
    }

    fun archiveVendor(vendorId: Long, isArchived: Boolean) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            hisabRepository.archiveVendor(vendorId, user.id, isArchived)
            _snackbarMessage.emit(if (isArchived) "ভেন্ডর আর্কাইভ করা হয়েছে" else "ভেন্ডর সক্রিয় করা হয়েছে")
        }
    }

    fun addShopCreditPurchase(
        vendorId: Long,
        vendorName: String,
        accountId: Long,
        amount: Double,
        dueDate: Long?,
        note: String,
        phone: String,
        locationNote: String,
        categoryTag: String = ""
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            hisabRepository.addShopCreditPurchase(vendorId, vendorName, user.id, amount, dueDate, note, phone, locationNote, categoryTag)
            _snackbarMessage.emit("বাকিতে ক্রয়ের হিসাব সংরক্ষণ করা হয়েছে")
        }
    }

    fun settleShopCredit(vendorId: Long, accountId: Long, amount: Double, note: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            hisabRepository.settleShopCredit(vendorId, user.id, accountId, amount, note)
            _snackbarMessage.emit("বাকি পরিশোধ সম্পন্ন হয়েছে")
        }
    }

    // -------------------------------------------------------------
    // RECURRING RULES ACTIONS
    // -------------------------------------------------------------

    fun addRecurringRule(
        accountId: Long,
        categoryId: Long?,
        amount: Double,
        type: TransactionType,
        frequency: Frequency,
        startDate: Long,
        endDate: Long?,
        note: String
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val rule = RecurringRule(
                userId = user.id,
                accountId = accountId,
                categoryId = categoryId,
                amount = amount,
                type = type,
                frequency = frequency,
                startDate = startDate,
                endDate = endDate,
                note = note
            )
            hisabRepository.addRecurringRule(rule)
            _snackbarMessage.emit("স্বয়ংক্রিয় লেনদেন রুল যুক্ত করা হয়েছে")
        }
    }

    fun togglePauseRecurringRule(id: Long, isCurrentlyPaused: Boolean) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            hisabRepository.setRecurringPaused(id, user.id, !isCurrentlyPaused)
        }
    }

    fun deleteRecurringRule(id: Long) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            hisabRepository.deleteRecurringRule(id, user.id)
            _snackbarMessage.emit("স্বয়ংক্রিয় লেনদেন রুল মুছে ফেলা হয়েছে")
        }
    }

    // -------------------------------------------------------------
    // SETTINGS & SECURITY ACTIONS
    // -------------------------------------------------------------

    fun toggleHideBalances() {
        val curr = _settings.value
        val updated = curr.copy(hideBalances = !curr.hideBalances)
        updateSettings(updated)
    }

    fun toggleLanguage() {
        val curr = _settings.value
        val newLang = if (curr.language == "bn") "en" else "bn"
        appPrefs.edit().putString("language", newLang).apply()
        val user = currentUser.value
        val updated = curr.copy(language = newLang, userId = user?.id ?: "")
        _settings.value = updated
        viewModelScope.launch {
            hisabRepository.updateSettings(updated)
        }
    }

    fun toggleNumeralSystem() {
        val curr = _settings.value
        val newNumeral = if (curr.numeralSystem == "bn") "en" else "bn"
        appPrefs.edit().putString("numeral_system", newNumeral).apply()
        val user = currentUser.value
        val updated = curr.copy(numeralSystem = newNumeral, userId = user?.id ?: "")
        _settings.value = updated
        viewModelScope.launch {
            hisabRepository.updateSettings(updated)
        }
    }

    fun updateCurrency(currencyCode: String, symbol: String) {
        val curr = _settings.value
        val updated = curr.copy(defaultCurrency = currencyCode, currencySymbol = symbol)
        updateSettings(updated)
    }

    fun setPin(pin: String) {
        val curr = _settings.value
        val updated = curr.copy(pinEnabled = pin.isNotBlank(), pinCode = pin)
        updateSettings(updated)
        _isAppLocked.value = false
    }

    fun disablePin() {
        val curr = _settings.value
        val updated = curr.copy(pinEnabled = false, pinCode = "")
        updateSettings(updated)
        _isAppLocked.value = false
    }

    fun verifyPin(inputPin: String): Boolean {
        if (_settings.value.pinCode == inputPin) {
            _isAppLocked.value = false
            return true
        }
        return false
    }

    fun lockApp() {
        if (_settings.value.pinEnabled && _settings.value.pinCode.isNotBlank()) {
            _isAppLocked.value = true
        }
    }

    private fun updateSettings(updated: UserSettings) {
        appPrefs.edit()
            .putString("language", updated.language)
            .putString("numeral_system", updated.numeralSystem)
            .apply()
        _settings.value = updated
        val user = currentUser.value
        val target = if (user != null) updated.copy(userId = user.id) else updated
        viewModelScope.launch {
            hisabRepository.updateSettings(target)
        }
    }

    // -------------------------------------------------------------
    // REPORTS COMPUTATION
    // -------------------------------------------------------------

    fun computePeriodReport(startDate: Long, endDate: Long): PeriodReport {
        val txs = _transactions.value.filter {
            it.transaction.dateTimestamp in startDate..endDate
        }

        val incomeTxs = txs.filter { it.transaction.type == TransactionType.INCOME }
        val expenseTxs = txs.filter { it.transaction.type == TransactionType.EXPENSE }

        val totalIncome = incomeTxs.sumOf { it.transaction.amount }
        val totalExpense = expenseTxs.sumOf { it.transaction.amount }
        val netSavings = totalIncome - totalExpense
        val savingsRate = if (totalIncome > 0) ((netSavings / totalIncome) * 100).toFloat().coerceIn(-100f, 100f) else 0f

        val catMap = expenseTxs
            .filter { it.category != null }
            .groupBy { it.category!! }
            .mapValues { it.value.sumOf { tx -> tx.transaction.amount } }
            .toList()
            .sortedByDescending { it.second }

        // Daily breakdown for trend
        val dailyMap = expenseTxs
            .groupBy { Formatters.formatDate(it.transaction.dateTimestamp, false) }
            .mapValues { it.value.sumOf { tx -> tx.transaction.amount } }
            .toList()

        return PeriodReport(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netSavings = netSavings,
            savingsRate = savingsRate,
            categoryBreakdown = catMap,
            dailyBreakdown = dailyMap,
            transactionCount = txs.size
        )
    }

    // -------------------------------------------------------------
    // DATA EXPORT & IMPORT
    // -------------------------------------------------------------

    fun exportTransactionsCsv(): String {
        return CsvExporterImporter.exportToCsv(_transactions.value)
    }

    fun generateFinancialSummary(): String {
        val isBn = _settings.value.language == "bn"
        val currSymbol = _settings.value.currencySymbol
        val useBnDigits = _settings.value.numeralSystem == "bn"

        val report = computePeriodReport(
            Formatters.getStartAndEndOfMonth(Formatters.getCurrentMonthYear()).first,
            Formatters.getStartAndEndOfMonth(Formatters.getCurrentMonthYear()).second
        )

        val totalBalStr = Formatters.formatAmount(_totalBalance.value, currSymbol, useBnDigits)
        val incomeStr = Formatters.formatAmount(report.totalIncome, currSymbol, useBnDigits)
        val expenseStr = Formatters.formatAmount(report.totalExpense, currSymbol, useBnDigits)
        val savingsStr = Formatters.formatAmount(report.netSavings, currSymbol, useBnDigits)

        return if (isBn) {
            """
            📊 হিসাব (Hisab) - মাসিক আর্থিক বিবরণী (${Formatters.formatMonthYear(Formatters.getCurrentMonthYear(), true)})
            ─────────────────────────────
            💰 মোট ব্যালেন্স: $totalBalStr
            📈 মোট আয়: $incomeStr
            📉 মোট খরচ: $expenseStr
            🌱 নিট সঞ্চয়: $savingsStr
            ─────────────────────────────
            অ্যাকাউন্টসমূহ:
            ${_accountsWithBalances.value.joinToString("\n") { "• ${it.account.name}: ${Formatters.formatAmount(it.balance, currSymbol, useBnDigits)}" }}
            """.trimIndent()
        } else {
            """
            📊 Hisab Financial Summary (${Formatters.formatMonthYear(Formatters.getCurrentMonthYear(), false)})
            ─────────────────────────────
            💰 Total Balance: $totalBalStr
            📈 Total Income: $incomeStr
            📉 Total Expense: $expenseStr
            🌱 Net Savings: $savingsStr
            ─────────────────────────────
            Accounts:
            ${_accountsWithBalances.value.joinToString("\n") { "• ${it.account.name}: ${Formatters.formatAmount(it.balance, currSymbol, useBnDigits)}" }}
            """.trimIndent()
        }
    }

    fun importTransactionsFromCsv(csvContent: String) {
        val user = currentUser.value ?: return
        val defaultAcc = _accountsWithBalances.value.firstOrNull()?.account?.id ?: return
        viewModelScope.launch {
            try {
                val parsed = CsvExporterImporter.parseCsv(csvContent, user.id, defaultAcc)
                if (parsed.isNotEmpty()) {
                    hisabRepository.importTransactions(parsed)
                    _snackbarMessage.emit("${parsed.size} টি লেনদেন সফলভাবে যুক্ত হয়েছে")
                } else {
                    _snackbarMessage.emit("কোনো সঠিক লেনদেনের তথ্য পাওয়া যায়নি")
                }
            } catch (e: Exception) {
                _snackbarMessage.emit("CSV ফাইল পড়তে সমস্যা হয়েছে")
            }
        }
    }

    fun clearAllData() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            hisabRepository.clearAllData(user.id)
            _snackbarMessage.emit("সব হিসাব মুছে ফেলা হয়েছে")
        }
    }

    // -------------------------------------------------------------
    // IN-APP UPDATE ACTIONS
    // -------------------------------------------------------------

    fun checkForUpdates(isManual: Boolean = false) {
        viewModelScope.launch {
            if (isManual) {
                _updateUiState.value = UpdateUiState.Checking
            }
            val currentVersion = VersionUtils.cleanVersion(com.plusemon.hisab.BuildConfig.VERSION_NAME)
            val release = updateRepository.fetchLatestRelease()

            if (release != null && VersionUtils.isVersionNewer(currentVersion, release.version)) {
                val dismissedVersion = updateManager.getDismissedVersion()
                if (!isManual && release.version == dismissedVersion) {
                    _updateUiState.value = UpdateUiState.Idle
                } else {
                    _updateUiState.value = UpdateUiState.Available(release, isManual)
                }
            } else {
                if (isManual) {
                    _updateUiState.value = UpdateUiState.UpToDate(currentVersion)
                } else {
                    _updateUiState.value = UpdateUiState.Idle
                }
            }
        }
    }

    fun dismissUpdate(version: String) {
        updateManager.saveDismissedVersion(version)
        _updateUiState.value = UpdateUiState.Idle
    }

    fun dismissUpdateState() {
        _updateUiState.value = UpdateUiState.Idle
    }

    fun downloadAndInstallUpdate(updateInfo: UpdateInfo) {
        viewModelScope.launch {
            _updateUiState.value = UpdateUiState.Downloading(updateInfo, 0f)
            try {
                updateManager.downloadApk(updateInfo).collect { progress ->
                    _updateUiState.value = UpdateUiState.Downloading(updateInfo, progress)
                }
                promptInstallOrRequestPermission(updateInfo)
            } catch (e: Exception) {
                _snackbarMessage.emit("Download failed: ${e.message}")
                _updateUiState.value = UpdateUiState.Idle
            }
        }
    }

    fun promptInstallOrRequestPermission(updateInfo: UpdateInfo) {
        if (!updateManager.canInstallUnknownApps()) {
            _updateUiState.value = UpdateUiState.PermissionNeeded(updateInfo)
        } else {
            val launched = updateManager.promptInstallApk(updateInfo.version)
            if (launched) {
                _updateUiState.value = UpdateUiState.Downloaded(updateInfo)
            } else {
                _updateUiState.value = UpdateUiState.PermissionNeeded(updateInfo)
            }
        }
    }

    fun openInstallPermissionSettings() {
        updateManager.openInstallPermissionSettings()
    }

    fun retryInstall(updateInfo: UpdateInfo) {
        promptInstallOrRequestPermission(updateInfo)
    }
}
