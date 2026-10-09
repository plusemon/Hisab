package com.plusemon.hisab.ui.screens.transactions

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.plusemon.hisab.data.model.Category
import com.plusemon.hisab.data.model.TransactionRecord
import com.plusemon.hisab.data.model.TransactionType
import com.plusemon.hisab.data.model.TransactionWithDetails
import com.plusemon.hisab.data.model.UserAccount
import com.plusemon.hisab.domain.util.Formatters
import com.plusemon.hisab.domain.util.Localization
import com.plusemon.hisab.ui.components.CategoryIconBadge
import com.plusemon.hisab.ui.components.DeleteConfirmationDialog
import com.plusemon.hisab.ui.components.DetailTopAppBar
import com.plusemon.hisab.ui.components.getIconByName
import com.plusemon.hisab.ui.components.parseColorHex
import com.plusemon.hisab.ui.theme.ExpenseRed
import com.plusemon.hisab.ui.theme.IncomeGreen
import com.plusemon.hisab.ui.theme.TransferBlue
import com.plusemon.hisab.ui.viewmodel.HisabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    viewModel: HisabViewModel,
    existingTransaction: TransactionWithDetails? = null,
    initialType: TransactionType = TransactionType.EXPENSE,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val isBn = settings.language == "bn"
    val currSymbol = settings.currencySymbol

    val accountsWithBalances by viewModel.accountsWithBalances.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var transactionType by remember {
        mutableStateOf(existingTransaction?.transaction?.type ?: initialType)
    }

    var amountText by remember {
        mutableStateOf(existingTransaction?.transaction?.amount?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }

    var feeText by remember {
        mutableStateOf(existingTransaction?.transaction?.fee?.let { if (it > 0) it.toString() else "" } ?: "")
    }

    var exchangeRateText by remember {
        mutableStateOf(existingTransaction?.transaction?.exchangeRate?.let { if (it != 1.0) it.toString() else "1.0" } ?: "1.0")
    }

    val availableAccounts = accountsWithBalances.map { it.account }

    var selectedAccountId by remember {
        mutableStateOf(
            existingTransaction?.transaction?.accountId
                ?: availableAccounts.firstOrNull()?.id
                ?: 0L
        )
    }

    var selectedToAccountId by remember {
        mutableStateOf(
            existingTransaction?.transaction?.toAccountId
                ?: availableAccounts.getOrNull(1)?.id
                ?: availableAccounts.firstOrNull()?.id
                ?: 0L
        )
    }

    val typeCategories = remember(categories, transactionType) {
        categories.filter { it.type == transactionType }
    }

    var selectedCategoryId by remember {
        mutableStateOf<Long?>(
            existingTransaction?.transaction?.categoryId
                ?: typeCategories.firstOrNull()?.id
        )
    }

    // Ensure data is seeded if empty
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.ensureDefaultData()
    }

    // Keep selectedAccountId in sync as accounts load reactively
    androidx.compose.runtime.LaunchedEffect(availableAccounts) {
        if (availableAccounts.isNotEmpty()) {
            if (selectedAccountId <= 0L || availableAccounts.none { it.id == selectedAccountId }) {
                selectedAccountId = availableAccounts.first().id
            }
            if (selectedToAccountId <= 0L || availableAccounts.none { it.id == selectedToAccountId }) {
                selectedToAccountId = availableAccounts.getOrNull(1)?.id ?: availableAccounts.first().id
            }
        }
    }

    // Keep selectedCategoryId in sync when switching types or when categories load reactively
    androidx.compose.runtime.LaunchedEffect(typeCategories, transactionType) {
        if (transactionType != TransactionType.TRANSFER) {
            if (selectedCategoryId == null || typeCategories.none { it.id == selectedCategoryId }) {
                selectedCategoryId = typeCategories.firstOrNull()?.id
            }
        } else {
            selectedCategoryId = null
        }
    }

    var note by remember {
        mutableStateOf(existingTransaction?.transaction?.note ?: "")
    }

    var receiptUriStr by remember {
        mutableStateOf(existingTransaction?.transaction?.receiptUri)
    }

    var dateTimestamp by remember {
        mutableStateOf(existingTransaction?.transaction?.dateTimestamp ?: System.currentTimeMillis())
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation && existingTransaction != null) {
        val amountStr = Formatters.formatAmount(existingTransaction.transaction.amount, currSymbol, isBn)
        val catOrAcc = existingTransaction.category?.localizedName(isBn) ?: existingTransaction.account.name
        val detail = "$amountStr • $catOrAcc${if (existingTransaction.transaction.note.isNotBlank()) " • " + existingTransaction.transaction.note else ""}"
        DeleteConfirmationDialog(
            title = if (isBn) "লেনদেন মুছে ফেলবেন?" else "Delete Transaction?",
            message = if (isBn) "আপনি কি নিশ্চিত যে এই লেনদেনটি মুছে ফেলতে চান? লেনদেনটি মুছে ফেললে ব্যালেন্স স্বয়ংক্রিয়ভাবে পুনর্গণনা করা হবে।" else "Are you sure you want to delete this transaction? Balance will be updated automatically.",
            itemDetail = detail,
            isBangla = isBn,
            onConfirm = {
                viewModel.deleteTransaction(existingTransaction.transaction)
                showDeleteConfirmation = false
                onNavigateBack()
            },
            onDismiss = {
                showDeleteConfirmation = false
            }
        )
    }

    // Photo picker launcher (Zero-permission Android Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            receiptUriStr = uri.toString()
        }
    }

    Scaffold(
        topBar = {
            val titleText = if (existingTransaction == null) {
                when (transactionType) {
                    TransactionType.EXPENSE -> Localization.getString(Localization.Key.ADD_EXPENSE, isBn)
                    TransactionType.INCOME -> Localization.getString(Localization.Key.ADD_INCOME, isBn)
                    TransactionType.TRANSFER -> Localization.getString(Localization.Key.TRANSFER, isBn)
                }
            } else {
                if (isBn) "লেনদেন সম্পাদন" else "Edit Transaction"
            }
            DetailTopAppBar(
                title = titleText,
                onNavigateBack = onNavigateBack,
                actions = {
                    if (existingTransaction != null) {
                        IconButton(
                            onClick = {
                                showDeleteConfirmation = true
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("delete_transaction_btn")
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = if (isBn) "মুছুন" else "Delete",
                                tint = ExpenseRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Type Selector Tabs (Expense / Income / Transfer)
            if (existingTransaction == null) {
                TabRow(
                    selectedTabIndex = when (transactionType) {
                        TransactionType.EXPENSE -> 0
                        TransactionType.INCOME -> 1
                        TransactionType.TRANSFER -> 2
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .padding(bottom = 16.dp)
                ) {
                    Tab(
                        selected = transactionType == TransactionType.EXPENSE,
                        onClick = {
                            transactionType = TransactionType.EXPENSE
                            selectedCategoryId = categories.firstOrNull { it.type == TransactionType.EXPENSE }?.id
                        },
                        text = {
                            Text(
                                text = Localization.getString(Localization.Key.ADD_EXPENSE, isBn),
                                fontWeight = FontWeight.Bold,
                                color = if (transactionType == TransactionType.EXPENSE) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    Tab(
                        selected = transactionType == TransactionType.INCOME,
                        onClick = {
                            transactionType = TransactionType.INCOME
                            selectedCategoryId = categories.firstOrNull { it.type == TransactionType.INCOME }?.id
                        },
                        text = {
                            Text(
                                text = Localization.getString(Localization.Key.ADD_INCOME, isBn),
                                fontWeight = FontWeight.Bold,
                                color = if (transactionType == TransactionType.INCOME) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    Tab(
                        selected = transactionType == TransactionType.TRANSFER,
                        onClick = {
                            transactionType = TransactionType.TRANSFER
                            selectedCategoryId = null
                        },
                        text = {
                            Text(
                                text = Localization.getString(Localization.Key.TRANSFER, isBn),
                                fontWeight = FontWeight.Bold,
                                color = if (transactionType == TransactionType.TRANSFER) TransferBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // Big Amount Input Card (Flat card with border)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = Localization.getString(Localization.Key.AMOUNT, isBn),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currSymbol,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (transactionType) {
                                TransactionType.EXPENSE -> ExpenseRed
                                TransactionType.INCOME -> IncomeGreen
                                TransactionType.TRANSFER -> TransferBlue
                            }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = amountText,
                            onValueChange = {
                                if (it.isEmpty() || it.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                                    amountText = it
                                    errorMessage = null
                                }
                            },
                            placeholder = { Text("0.00", fontSize = 32.sp, fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = when (transactionType) {
                                    TransactionType.EXPENSE -> ExpenseRed
                                    TransactionType.INCOME -> IncomeGreen
                                    TransactionType.TRANSFER -> TransferBlue
                                }
                            ),
                            modifier = Modifier
                                .width(220.dp)
                                .testTag("tx_amount_input")
                        )
                    }

                    // Quick amount chips (+50, +100, +500, +1000)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(50, 100, 500, 1000, 5000).forEach { quickVal ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        val currentVal = amountText.toDoubleOrNull() ?: 0.0
                                        val nextVal = currentVal + quickVal
                                        amountText = if (nextVal % 1.0 == 0.0) nextVal.toInt().toString() else nextVal.toString()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "+$quickVal",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TRANSFER SECTION
            if (transactionType == TransactionType.TRANSFER) {
                Text(
                    text = Localization.getString(Localization.Key.FROM_ACCOUNT, isBn),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                AccountSelectorChips(
                    accounts = availableAccounts,
                    selectedId = selectedAccountId,
                    onSelect = { selectedAccountId = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = Localization.getString(Localization.Key.TO_ACCOUNT, isBn),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                AccountSelectorChips(
                    accounts = availableAccounts,
                    selectedId = selectedToAccountId,
                    onSelect = { selectedToAccountId = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Optional Transfer Fee
                OutlinedTextField(
                    value = feeText,
                    onValueChange = { feeText = it },
                    label = { Text(Localization.getString(Localization.Key.FEE, isBn)) },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // EXPENSE / INCOME SECTION
                Text(
                    text = Localization.getString(Localization.Key.ACCOUNT, isBn),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (availableAccounts.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { viewModel.ensureDefaultData() }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isBn) "কোনো অ্যাকাউন্ট পাওয়া যায়নি" else "No account found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isBn) "ডিফল্ট অ্যাকাউন্ট তৈরি করতে এখানে ট্যাপ করুন" else "Tap here to load default accounts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                } else {
                    AccountSelectorChips(
                        accounts = availableAccounts,
                        selectedId = selectedAccountId,
                        onSelect = { selectedAccountId = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Grid Selector
                Text(
                    text = Localization.getString(Localization.Key.CATEGORY, isBn),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (typeCategories.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { viewModel.ensureDefaultData() }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isBn) "কোনো ক্যাটাগরি পাওয়া যায়নি" else "No categories found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isBn) "ডিফল্ট ক্যাটাগরি লোড করতে এখানে ট্যাপ করুন" else "Tap here to reload default categories",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                } else {
                    CategoryGridSelector(
                        categories = typeCategories,
                        selectedCategoryId = selectedCategoryId,
                        isBangla = isBn,
                        onSelect = { selectedCategoryId = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date & Time Display
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = Formatters.formatDateTime(dateTimestamp, isBn),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Note field
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(Localization.getString(Localization.Key.NOTE, isBn)) },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_note_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Receipt Photo Attachment
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Localization.getString(Localization.Key.RECEIPT, isBn), fontWeight = FontWeight.Bold)
                }

                if (receiptUriStr != null) {
                    IconButton(onClick = { receiptUriStr = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Remove receipt", tint = ExpenseRed)
                    }
                }
            }

            if (receiptUriStr != null) {
                AsyncImage(
                    model = receiptUriStr,
                    contentDescription = "Receipt Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount <= 0.0) {
                        errorMessage = if (isBn) "সঠিক টাকার পরিমাণ লিখুন" else "Please enter a valid amount"
                        return@Button
                    }

                    val effectiveAccountId = if (selectedAccountId > 0L) selectedAccountId else availableAccounts.firstOrNull()?.id ?: 0L
                    if (effectiveAccountId <= 0L) {
                        viewModel.ensureDefaultData()
                        errorMessage = if (isBn) "অনুগ্রহ করে একটি অ্যাকাউন্ট নির্বাচন করুন বা তৈরি করুন" else "Please select or create an account"
                        return@Button
                    }

                    val effectiveToAccountId = if (selectedToAccountId > 0L) selectedToAccountId else availableAccounts.getOrNull(1)?.id ?: effectiveAccountId

                    if (transactionType == TransactionType.TRANSFER && effectiveAccountId == effectiveToAccountId) {
                        errorMessage = if (isBn) "একই অ্যাকাউন্টে স্থানান্তর সম্ভব নয়" else "Source and target accounts must be different"
                        return@Button
                    }

                    val fee = feeText.toDoubleOrNull() ?: 0.0
                    val exchangeRate = exchangeRateText.toDoubleOrNull() ?: 1.0
                    val effectiveCategoryId = if (transactionType == TransactionType.TRANSFER) null else (selectedCategoryId ?: typeCategories.firstOrNull()?.id)

                    if (existingTransaction == null) {
                        viewModel.addTransaction(
                            accountId = effectiveAccountId,
                            categoryId = effectiveCategoryId,
                            toAccountId = if (transactionType == TransactionType.TRANSFER) effectiveToAccountId else null,
                            amount = amount,
                            fee = fee,
                            type = transactionType,
                            dateTimestamp = dateTimestamp,
                            note = note,
                            receiptUri = receiptUriStr,
                            exchangeRate = exchangeRate
                        )
                    } else {
                        viewModel.updateTransaction(
                            id = existingTransaction.transaction.id,
                            accountId = effectiveAccountId,
                            categoryId = effectiveCategoryId,
                            toAccountId = if (transactionType == TransactionType.TRANSFER) effectiveToAccountId else null,
                            amount = amount,
                            fee = fee,
                            type = transactionType,
                            dateTimestamp = dateTimestamp,
                            note = note,
                            receiptUri = receiptUriStr,
                            exchangeRate = exchangeRate
                        )
                    }
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_transaction_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (transactionType) {
                        TransactionType.EXPENSE -> ExpenseRed
                        TransactionType.INCOME -> IncomeGreen
                        TransactionType.TRANSFER -> TransferBlue
                    }
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Localization.getString(Localization.Key.SAVE, isBn),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun AccountSelectorChips(
    accounts: List<UserAccount>,
    selectedId: Long,
    onSelect: (Long) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(accounts, key = { it.id }) { acc ->
            val isSelected = acc.id == selectedId
            val accColor = parseColorHex(acc.colorHex)

            Surface(
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelect(acc.id) }
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) accColor else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(12.dp)
                    ),
                color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getIconByName(acc.iconName),
                        contentDescription = null,
                        tint = accColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = acc.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryGridSelector(
    categories: List<Category>,
    selectedCategoryId: Long?,
    isBangla: Boolean,
    onSelect: (Long) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val chunked = categories.chunked(3)
        chunked.forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowCategories.forEach { cat ->
                    val isSelected = cat.id == selectedCategoryId
                    val catColor = parseColorHex(cat.colorHex)

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(cat.id) }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) catColor else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("category_chip_${cat.id}"),
                        color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CategoryIconBadge(
                                iconName = cat.iconName,
                                colorHex = cat.colorHex,
                                size = 36.dp,
                                iconSize = 20.dp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cat.localizedName(isBangla),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                repeat(3 - rowCategories.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
