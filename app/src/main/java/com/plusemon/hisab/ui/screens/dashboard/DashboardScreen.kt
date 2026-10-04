package com.plusemon.hisab.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plusemon.hisab.data.model.DebtType
import com.plusemon.hisab.data.model.TransactionType
import com.plusemon.hisab.data.model.TransactionWithDetails
import com.plusemon.hisab.domain.util.Formatters
import com.plusemon.hisab.domain.util.Localization
import com.plusemon.hisab.ui.components.CategoryIconBadge
import com.plusemon.hisab.ui.components.CurrencyAmountText
import com.plusemon.hisab.ui.components.getIconByName
import com.plusemon.hisab.ui.components.parseColorHex
import com.plusemon.hisab.ui.screens.nlp.QuickEntryDialog
import com.plusemon.hisab.ui.theme.ExpenseRed
import com.plusemon.hisab.ui.theme.IncomeGreen
import com.plusemon.hisab.ui.theme.TransferBlue
import com.plusemon.hisab.ui.viewmodel.AccountWithBalance
import com.plusemon.hisab.ui.viewmodel.BudgetStatus
import com.plusemon.hisab.ui.viewmodel.HisabViewModel

@Composable
fun DashboardScreen(
    viewModel: HisabViewModel,
    onNavigateToAddTransaction: (TransactionType) -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToDebts: () -> Unit,
    onTransactionClick: (TransactionWithDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val isBn = settings.language == "bn"
    val useBnDigits = settings.numeralSystem == "bn"
    val hideBalances = settings.hideBalances
    val currSymbol = settings.currencySymbol

    val totalBalance by viewModel.totalBalance.collectAsState()
    val accountsWithBalances by viewModel.accountsWithBalances.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val categoryProgressList by viewModel.categoryProgressList.collectAsState()
    val overallBudgetProgress by viewModel.overallBudgetProgress.collectAsState()
    val insights by viewModel.insights.collectAsState()
    val allDebts by viewModel.loansDebts.collectAsState()
    val shopCreditPurchases by viewModel.shopCreditPurchases.collectAsState()

    val totalReceivable = allDebts.filter { it.type == DebtType.OWED_TO_ME && !it.isSettled }.sumOf { it.remainingAmount }
    val totalPayable = allDebts.filter { it.type == DebtType.I_OWE && !it.isSettled }.sumOf { it.remainingAmount }
    val totalShopOwed = shopCreditPurchases.filter { !it.isSettled }.sumOf { it.remainingAmount }

    var showNlpDialog by remember { mutableStateOf(false) }

    if (showNlpDialog) {
        QuickEntryDialog(
            viewModel = viewModel,
            isBangla = isBn,
            onDismiss = { showNlpDialog = false }
        )
    }

    val (monthStart, monthEnd) = Formatters.getStartAndEndOfMonth(Formatters.getCurrentMonthYear())
    val monthTransactions = transactions.filter { it.transaction.dateTimestamp in monthStart..monthEnd }
    val monthIncome = monthTransactions.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
    val monthExpense = monthTransactions.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        // Natural Language Quick Entry Search Pill
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable { showNlpDialog = true }
                .testTag("nlp_quick_entry_pill"),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isBn) "সহজ ভাষায় হিসাব লিখুন (যেমন: বাজার ১২০০)..." else "Type natural entry (e.g. lunch 250 cash)...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Lending & Shop Credit Informational Summary Card
        if (totalReceivable > 0 || totalPayable > 0 || totalShopOwed > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onNavigateToDebts() }
                    .testTag("dashboard_debts_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "ধার ও দোকান বাকি (তথ্য)" else "Lending & Shop Credit",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = if (isBn) "পাওনা" else "Receivable", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            CurrencyAmountText(amount = totalReceivable, currencySymbol = currSymbol, useBanglaDigits = useBnDigits, hideBalances = hideBalances, color = IncomeGreen, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = if (isBn) "দেনা" else "Payable", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            CurrencyAmountText(amount = totalPayable, currencySymbol = currSymbol, useBanglaDigits = useBnDigits, hideBalances = hideBalances, color = ExpenseRed, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = if (isBn) "দোকান বাকি" else "Shop Credit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            CurrencyAmountText(amount = totalShopOwed, currencySymbol = currSymbol, useBanglaDigits = useBnDigits, hideBalances = hideBalances, color = ExpenseRed, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Hero Total Net Worth Card
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Localization.getString(Localization.Key.TOTAL_BALANCE, isBn),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = Formatters.formatMonthYear(Formatters.getCurrentMonthYear(), isBn),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Big Number
                    CurrencyAmountText(
                        amount = totalBalance,
                        currencySymbol = currSymbol,
                        useBanglaDigits = useBnDigits,
                        hideBalances = hideBalances,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.testTag("hero_total_balance_text")
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Income & Expense Sub-Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Income Block
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(IncomeGreen.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = Localization.getString(Localization.Key.INCOME_THIS_MONTH, isBn),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                                        fontSize = 10.sp
                                    )
                                    CurrencyAmountText(
                                        amount = monthIncome,
                                        currencySymbol = currSymbol,
                                        useBanglaDigits = useBnDigits,
                                        hideBalances = hideBalances,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Expense Block
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(ExpenseRed.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = Localization.getString(Localization.Key.EXPENSES_THIS_MONTH, isBn),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                                        fontSize = 10.sp
                                    )
                                    CurrencyAmountText(
                                        amount = monthExpense,
                                        currencySymbol = currSymbol,
                                        useBanglaDigits = useBnDigits,
                                        hideBalances = hideBalances,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Fast Action Buttons: [+ Expense], [+ Income], [⇄ Transfer]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onNavigateToAddTransaction(TransactionType.EXPENSE) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("fast_add_expense_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = Localization.getString(Localization.Key.ADD_EXPENSE, isBn),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Button(
                onClick = { onNavigateToAddTransaction(TransactionType.INCOME) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("fast_add_income_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = Localization.getString(Localization.Key.ADD_INCOME, isBn),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Button(
                onClick = { onNavigateToAddTransaction(TransactionType.TRANSFER) },
                modifier = Modifier
                    .weight(1.1f)
                    .height(48.dp)
                    .testTag("fast_transfer_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = TransferBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = Localization.getString(Localization.Key.TRANSFER, isBn),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        // Budget Warning Alert Banner (if any category exceeded)
        val budgetWarnings = categoryProgressList.filter { it.status == BudgetStatus.EXCEEDED || it.status == BudgetStatus.WARNING }
        if (budgetWarnings.isNotEmpty()) {
            val topWarning = budgetWarnings.first()
            val isExceeded = topWarning.status == BudgetStatus.EXCEEDED
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { onNavigateToBudgets() },
                colors = CardDefaults.cardColors(
                    containerColor = if (isExceeded) ExpenseRed.copy(alpha = 0.12f) else Color(0xFFF59E0B).copy(alpha = 0.12f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isExceeded) ExpenseRed else Color(0xFFD97706),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isExceeded) Localization.getString(Localization.Key.BUDGET_EXCEEDED, isBn)
                            else Localization.getString(Localization.Key.BUDGET_WARNING, isBn),
                            fontWeight = FontWeight.Bold,
                            color = if (isExceeded) ExpenseRed else Color(0xFFD97706),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "${topWarning.category.localizedName(isBn)}: ${Formatters.formatAmount(topWarning.spentAmount, currSymbol, useBnDigits)} / ${Formatters.formatAmount(topWarning.budgetLimit ?: 0.0, currSymbol, useBnDigits)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Smart Spending Insights Card
        if (insights.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = insights.first(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // My Accounts Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Localization.getString(Localization.Key.MY_ACCOUNTS, isBn),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onNavigateToAccounts) {
                Text(Localization.getString(Localization.Key.SEE_ALL, isBn), fontSize = 13.sp)
            }
        }

        // Horizontal Carousel of Accounts
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(accountsWithBalances, key = { it.account.id }) { accWithBal ->
                AccountCarouselCard(
                    item = accWithBal,
                    currSymbol = currSymbol,
                    useBnDigits = useBnDigits,
                    hideBalances = hideBalances,
                    onClick = onNavigateToAccounts
                )
            }
        }

        // Recent Transactions Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Localization.getString(Localization.Key.RECENT_TRANSACTIONS, isBn),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onNavigateToTransactions) {
                Text(Localization.getString(Localization.Key.SEE_ALL, isBn), fontSize = 13.sp)
            }
        }

        val recentTxs = transactions.take(6)
        if (recentTxs.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Text(
                    text = Localization.getString(Localization.Key.EMPTY_TRANSACTIONS, isBn),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                recentTxs.forEach { txItem ->
                    TransactionRowItem(
                        item = txItem,
                        isBangla = isBn,
                        useBnDigits = useBnDigits,
                        hideBalances = hideBalances,
                        currencySymbol = currSymbol,
                        onClick = { onTransactionClick(txItem) }
                    )
                }
            }
        }
    }
}

@Composable
fun AccountCarouselCard(
    item: AccountWithBalance,
    currSymbol: String,
    useBnDigits: Boolean,
    hideBalances: Boolean,
    onClick: () -> Unit
) {
    val accColor = parseColorHex(item.account.colorHex)

    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
            .testTag("account_card_${item.account.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconByName(item.account.iconName),
                        contentDescription = null,
                        tint = accColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = item.account.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            CurrencyAmountText(
                amount = item.balance,
                currencySymbol = currSymbol,
                useBanglaDigits = useBnDigits,
                hideBalances = hideBalances,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (item.balance < 0) ExpenseRed else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun TransactionRowItem(
    item: TransactionWithDetails,
    isBangla: Boolean,
    useBnDigits: Boolean,
    hideBalances: Boolean,
    currencySymbol: String,
    onClick: () -> Unit
) {
    val t = item.transaction
    val (typeColor, sign) = when (t.type) {
        TransactionType.EXPENSE -> Pair(ExpenseRed, "-")
        TransactionType.INCOME -> Pair(IncomeGreen, "+")
        TransactionType.TRANSFER -> Pair(TransferBlue, "⇄")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("tx_item_${t.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            if (t.type == TransactionType.TRANSFER) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(TransferBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = TransferBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            } else if (item.category != null) {
                CategoryIconBadge(
                    iconName = item.category.iconName,
                    colorHex = item.category.colorHex,
                    size = 42.dp,
                    iconSize = 22.dp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main Info
            Column(modifier = Modifier.weight(1f)) {
                val title = when (t.type) {
                    TransactionType.TRANSFER -> "${item.account.name} → ${item.toAccount?.name ?: "Account"}"
                    else -> item.category?.localizedName(isBangla) ?: if (isBangla) "সাধারণ" else "General"
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )

                val subtext = if (t.note.isNotBlank()) t.note else item.account.name
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            // Amount & Date
            Column(horizontalAlignment = Alignment.End) {
                CurrencyAmountText(
                    amount = t.amount,
                    currencySymbol = currencySymbol,
                    useBanglaDigits = useBnDigits,
                    hideBalances = hideBalances,
                    prefix = sign,
                    color = typeColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = Formatters.formatDate(t.dateTimestamp, isBangla),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}
