package com.plusemon.hisab.ui.screens.transactions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plusemon.hisab.data.model.TransactionType
import com.plusemon.hisab.data.model.TransactionWithDetails
import com.plusemon.hisab.domain.util.Formatters
import com.plusemon.hisab.domain.util.Localization
import com.plusemon.hisab.ui.components.CategoryIconBadge
import com.plusemon.hisab.ui.components.CurrencyAmountText
import com.plusemon.hisab.ui.components.DeleteConfirmationDialog
import com.plusemon.hisab.ui.components.EmptyStateView
import com.plusemon.hisab.ui.components.getIconByName
import com.plusemon.hisab.ui.components.parseColorHex
import com.plusemon.hisab.ui.screens.dashboard.TransactionRowItem
import com.plusemon.hisab.ui.theme.ExpenseRed
import com.plusemon.hisab.ui.theme.IncomeGreen
import com.plusemon.hisab.ui.theme.TransferBlue
import com.plusemon.hisab.ui.viewmodel.HisabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: HisabViewModel,
    onNavigateToAddTransaction: () -> Unit,
    onTransactionClick: (TransactionWithDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val isBn = settings.language == "bn"
    val useBnDigits = settings.numeralSystem == "bn"
    val hideBalances = settings.hideBalances
    val currSymbol = settings.currencySymbol

    val allTransactions by viewModel.transactions.collectAsState()
    val accounts by viewModel.accountsWithBalances.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) }
    var selectedPeriodFilter by remember { mutableStateOf("THIS_MONTH") }
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    var txToDelete by remember { mutableStateOf<TransactionWithDetails?>(null) }

    if (txToDelete != null) {
        val target = txToDelete!!
        val amountStr = Formatters.formatAmount(target.transaction.amount, currSymbol, useBnDigits)
        val catOrAcc = target.category?.localizedName(isBn) ?: target.account.name
        val detail = "$amountStr • $catOrAcc${if (target.transaction.note.isNotBlank()) " • " + target.transaction.note else ""}"
        DeleteConfirmationDialog(
            title = if (isBn) "লেনদেন মুছে ফেলবেন?" else "Delete Transaction?",
            message = if (isBn) "আপনি কি নিশ্চিত যে এই লেনদেনটি মুছে ফেলতে চান? লেনদেনটি মুছে ফেললে মোট ব্যালেন্স সেই অনুযায়ী সমন্বয় হবে।" else "Are you sure you want to delete this transaction? Your total balance will be updated accordingly.",
            itemDetail = detail,
            isBangla = isBn,
            onConfirm = {
                viewModel.deleteTransaction(target.transaction)
                txToDelete = null
            },
            onDismiss = {
                txToDelete = null
            }
        )
    }

    // Filter logic including Account Filter
    val filteredTransactions = remember(allTransactions, searchQuery, selectedTypeFilter, selectedPeriodFilter, selectedAccountId) {
        val (start, end) = Formatters.getStartAndEndForPeriod(selectedPeriodFilter)
        allTransactions.filter { item ->
            val matchesPeriod = if (selectedPeriodFilter == "ALL") true else item.transaction.dateTimestamp in start..end
            val matchesType = selectedTypeFilter == null || item.transaction.type == selectedTypeFilter
            val matchesAccount = selectedAccountId == null ||
                    item.transaction.accountId == selectedAccountId ||
                    (item.transaction.type == TransactionType.TRANSFER && item.transaction.toAccountId == selectedAccountId)
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.lowercase()
                item.transaction.note.lowercase().contains(q) ||
                        (item.category?.nameBn?.lowercase()?.contains(q) == true) ||
                        (item.category?.nameEn?.lowercase()?.contains(q) == true) ||
                        item.account.name.lowercase().contains(q) ||
                        (item.toAccount?.name?.lowercase()?.contains(q) == true) ||
                        item.transaction.amount.toString().contains(q)
            }
            matchesPeriod && matchesType && matchesAccount && matchesSearch
        }
    }

    // Group by Date for Daily Subtotals
    val groupedByDate = remember(filteredTransactions) {
        filteredTransactions.groupBy {
            Formatters.formatDate(it.transaction.dateTimestamp, isBn)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_transaction_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(Localization.getString(Localization.Key.SEARCH_PLACEHOLDER, isBn), fontSize = 14.sp)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("tx_search_input")
            )

            // Period Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val periods = listOf(
                    Pair("THIS_MONTH", if (isBn) "এই মাস" else "This Month"),
                    Pair("LAST_MONTH", if (isBn) "গত মাস" else "Last Month"),
                    Pair("THIS_YEAR", if (isBn) "এই বছর" else "This Year"),
                    Pair("ALL", if (isBn) "সব সময়" else "All Time")
                )

                items(periods) { (key, label) ->
                    FilterChip(
                        selected = selectedPeriodFilter == key,
                        onClick = { selectedPeriodFilter = key },
                        label = { Text(label, fontSize = 12.sp) }
                    )
                }
            }

            // Type Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedTypeFilter == null,
                        onClick = { selectedTypeFilter = null },
                        label = { Text(Localization.getString(Localization.Key.ALL, isBn), fontSize = 12.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedTypeFilter == TransactionType.EXPENSE,
                        onClick = {
                            selectedTypeFilter = if (selectedTypeFilter == TransactionType.EXPENSE) null else TransactionType.EXPENSE
                        },
                        label = {
                            Text(
                                Localization.getString(Localization.Key.ADD_EXPENSE, isBn),
                                color = ExpenseRed,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedTypeFilter == TransactionType.INCOME,
                        onClick = {
                            selectedTypeFilter = if (selectedTypeFilter == TransactionType.INCOME) null else TransactionType.INCOME
                        },
                        label = {
                            Text(
                                Localization.getString(Localization.Key.ADD_INCOME, isBn),
                                color = IncomeGreen,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedTypeFilter == TransactionType.TRANSFER,
                        onClick = {
                            selectedTypeFilter = if (selectedTypeFilter == TransactionType.TRANSFER) null else TransactionType.TRANSFER
                        },
                        label = {
                            Text(
                                Localization.getString(Localization.Key.TRANSFER, isBn),
                                color = TransferBlue,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            // Account Filter Chips (Feature 1)
            if (accounts.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = selectedAccountId == null,
                            onClick = { selectedAccountId = null },
                            label = {
                                Text(
                                    text = if (isBn) "সব অ্যাকাউন্ট" else "All Accounts",
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                            },
                            modifier = Modifier.testTag("filter_account_all")
                        )
                    }

                    items(accounts, key = { it.account.id }) { accWithBal ->
                        val acc = accWithBal.account
                        val isSelected = selectedAccountId == acc.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedAccountId = if (isSelected) null else acc.id
                            },
                            label = {
                                Text(acc.name, fontSize = 12.sp)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = getIconByName(acc.iconName),
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = parseColorHex(acc.colorHex)
                                )
                            },
                            modifier = Modifier.testTag("filter_account_${acc.id}")
                        )
                    }
                }
            }

            // Transaction List or Empty State
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val isFiltering = selectedAccountId != null || selectedTypeFilter != null || searchQuery.isNotBlank() || selectedPeriodFilter != "THIS_MONTH"
                    EmptyStateView(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        title = Localization.getString(Localization.Key.EMPTY_TRANSACTIONS, isBn),
                        description = if (selectedAccountId != null) {
                            if (isBn) "এই অ্যাকাউন্টের জন্য কোনো লেনদেন পাওয়া যায়নি।" else "No transactions found for this account."
                        } else {
                            if (isBn) "পছন্দের ফিল্টারে বা সময়ে কোনো লেনদেন পাওয়া যায়নি।" else "No transactions match your current search or filter criteria."
                        },
                        actionLabel = if (isFiltering) {
                            if (isBn) "ফিল্টার রিসেট করুন" else "Reset Filters"
                        } else {
                            if (isBn) "+ নতুন হিসাব যোগ করুন" else "+ Add Transaction"
                        },
                        onActionClick = {
                            if (isFiltering) {
                                selectedAccountId = null
                                selectedTypeFilter = null
                                selectedPeriodFilter = "THIS_MONTH"
                                searchQuery = ""
                            } else {
                                onNavigateToAddTransaction()
                            }
                        }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    groupedByDate.forEach { (dateStr, itemsForDate) ->
                        val dayExpense = itemsForDate.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }
                        val dayIncome = itemsForDate.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }

                        // Date Header with Subtotals
                        item(key = "header_$dateStr") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (dayExpense > 0) {
                                        Text(
                                            text = "-${Formatters.formatAmount(dayExpense, currSymbol, useBnDigits, hideBalances)}",
                                             style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ExpenseRed
                                        )
                                    }
                                    if (dayIncome > 0) {
                                        Text(
                                            text = "+${Formatters.formatAmount(dayIncome, currSymbol, useBnDigits, hideBalances)}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = IncomeGreen
                                        )
                                    }
                                }
                            }
                        }

                        // List of Transactions for that Date
                        // Direct TransactionRowItem with onLongClick for safe deletion
                        // Allowing frictionless horizontal page swiping (Feature 2)
                        items(itemsForDate, key = { it.transaction.id }) { txItem ->
                            TransactionRowItem(
                                item = txItem,
                                isBangla = isBn,
                                useBnDigits = useBnDigits,
                                hideBalances = hideBalances,
                                currencySymbol = currSymbol,
                                onClick = { onTransactionClick(txItem) },
                                onLongClick = { txToDelete = txItem }
                            )
                        }
                    }
                }
            }
        }
    }
}
