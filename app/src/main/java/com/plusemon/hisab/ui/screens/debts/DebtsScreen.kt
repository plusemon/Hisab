package com.plusemon.hisab.ui.screens.debts

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.plusemon.hisab.data.model.Contact
import com.plusemon.hisab.data.model.DebtType
import com.plusemon.hisab.data.model.LoanDebt
import com.plusemon.hisab.data.model.ShopCreditPayment
import com.plusemon.hisab.data.model.ShopCreditPurchase
import com.plusemon.hisab.data.model.Vendor
import com.plusemon.hisab.domain.util.Formatters
import com.plusemon.hisab.domain.util.Localization
import com.plusemon.hisab.ui.components.CurrencyAmountText
import com.plusemon.hisab.ui.theme.ExpenseRed
import com.plusemon.hisab.ui.theme.IncomeGreen
import com.plusemon.hisab.ui.viewmodel.HisabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(
    viewModel: HisabViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val isBn = settings.language == "bn"
    val useBnDigits = settings.numeralSystem == "bn"
    val hideBalances = settings.hideBalances
    val currSymbol = settings.currencySymbol

    var hubTab by remember { mutableStateOf(0) } // 0: Lending & Borrowing (ধার), 1: Shop Credit (দোকান বাকি)

    val allDebts by viewModel.loansDebts.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val vendors by viewModel.vendors.collectAsState()
    val shopCreditPurchases by viewModel.shopCreditPurchases.collectAsState()
    val shopCreditPayments by viewModel.shopCreditPayments.collectAsState()
    val accounts by viewModel.accountsWithBalances.collectAsState()

    var selectedContactId by remember { mutableStateOf<Long?>(null) }
    var selectedVendorId by remember { mutableStateOf<Long?>(null) }

    var showAddLoanDialog by remember { mutableStateOf(false) }
    var recordingPaymentDebt by remember { mutableStateOf<LoanDebt?>(null) }

    var showAddCreditDialog by remember { mutableStateOf(false) }
    var settlingVendor by remember { mutableStateOf<Vendor?>(null) }

    val totalOwedToMe = allDebts.filter { it.type == DebtType.OWED_TO_ME && !it.isSettled }.sumOf { it.remainingAmount }
    val totalIOwe = allDebts.filter { it.type == DebtType.I_OWE && !it.isSettled }.sumOf { it.remainingAmount }
    val totalShopCreditOwed = shopCreditPurchases.filter { !it.isSettled }.sumOf { it.remainingAmount }

    val activeContact = contacts.find { it.id == selectedContactId }
    val activeVendor = vendors.find { it.id == selectedVendorId }

    if (showAddLoanDialog) {
        AddLoanEntryDialog(
            contacts = contacts,
            accounts = accounts,
            initialContactId = selectedContactId,
            isBangla = isBn,
            onDismiss = { showAddLoanDialog = false },
            onSave = { contactId, personName, accountId, amount, type, dueDate, note, phone ->
                viewModel.addLoanDebt(contactId, personName, accountId, amount, type, dueDate, note, phone)
                showAddLoanDialog = false
            }
        )
    }

    if (recordingPaymentDebt != null) {
        RecordPaymentDialog(
            debt = recordingPaymentDebt!!,
            accounts = accounts,
            isBangla = isBn,
            currencySymbol = currSymbol,
            onDismiss = { recordingPaymentDebt = null },
            onConfirm = { accountId, paymentAmount, note ->
                viewModel.recordDebtPayment(recordingPaymentDebt!!.id, accountId, paymentAmount, note)
                recordingPaymentDebt = null
            }
        )
    }

    if (showAddCreditDialog) {
        AddShopCreditDialog(
            vendors = vendors,
            purchases = shopCreditPurchases,
            initialVendorId = selectedVendorId,
            isBangla = isBn,
            onDismiss = { showAddCreditDialog = false },
            onSave = { vendorId, vendorName, amount, dueDate, note, phone, locationNote, tag ->
                viewModel.addShopCreditPurchase(vendorId, vendorName, 1L, amount, dueDate, note, phone, locationNote, tag)
                showAddCreditDialog = false
            }
        )
    }

    if (settlingVendor != null) {
        val vPurchases = shopCreditPurchases.filter { it.vendorId == settlingVendor!!.id }
        val vRemaining = vPurchases.filter { !it.isSettled }.sumOf { it.remainingAmount }
        SettleShopCreditDialog(
            vendor = settlingVendor!!,
            defaultAmount = vRemaining,
            accounts = accounts,
            isBangla = isBn,
            currencySymbol = currSymbol,
            onDismiss = { settlingVendor = null },
            onConfirm = { accountId, amount, note ->
                viewModel.settleShopCredit(settlingVendor!!.id, accountId, amount, note)
                settlingVendor = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            selectedContactId != null && activeContact != null -> activeContact.name
                            selectedVendorId != null && activeVendor != null -> activeVendor.name
                            else -> if (isBn) "ধার ও বাকি ব্যবস্থাপনা" else "Lending & Shop Credit"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (selectedContactId != null || selectedVendorId != null) {
                        IconButton(onClick = {
                            selectedContactId = null
                            selectedVendorId = null
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    } else {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedContactId == null && selectedVendorId == null) {
                FloatingActionButton(
                    onClick = {
                        if (hubTab == 0) showAddLoanDialog = true else showAddCreditDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_hub_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (selectedContactId == null && selectedVendorId == null) {
                TabRow(
                    selectedTabIndex = hubTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = hubTab == 0,
                        onClick = { hubTab = 0 },
                        text = {
                            Text(
                                text = if (isBn) "ধার (Lending)" else "Lending & Borrowing",
                                fontWeight = if (hubTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("tab_lending")
                    )
                    Tab(
                        selected = hubTab == 1,
                        onClick = { hubTab = 1 },
                        text = {
                            Text(
                                text = if (isBn) "দোকান বাকি (Shop Credit)" else "Shop Credit",
                                fontWeight = if (hubTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("tab_shop_credit")
                    )
                }

                if (hubTab == 0) {
                    DebtsOverviewContent(
                        contacts = contacts,
                        debts = allDebts,
                        totalReceivable = totalOwedToMe,
                        totalPayable = totalIOwe,
                        isBangla = isBn,
                        useBnDigits = useBnDigits,
                        hideBalances = hideBalances,
                        currencySymbol = currSymbol,
                        onContactClick = { contactId -> selectedContactId = contactId }
                    )
                } else {
                    ShopCreditOverviewContent(
                        vendors = vendors,
                        purchases = shopCreditPurchases,
                        totalOwed = totalShopCreditOwed,
                        isBangla = isBn,
                        useBnDigits = useBnDigits,
                        hideBalances = hideBalances,
                        currencySymbol = currSymbol,
                        onVendorClick = { vendorId -> selectedVendorId = vendorId },
                        onAddCreditClick = { showAddCreditDialog = true },
                        onArchiveVendor = { vId, archived -> viewModel.archiveVendor(vId, archived) }
                    )
                }
            } else if (selectedContactId != null && activeContact != null) {
                ContactLedgerContent(
                    contact = activeContact,
                    debts = allDebts.filter { it.contactId == activeContact.id || it.personName.equals(activeContact.name, true) },
                    isBangla = isBn,
                    useBnDigits = useBnDigits,
                    hideBalances = hideBalances,
                    currencySymbol = currSymbol,
                    onRecordPayment = { debt -> recordingPaymentDebt = debt },
                    onDeleteDebt = { debtId -> viewModel.deleteLoanDebt(debtId) },
                    onAddEntryClick = { showAddLoanDialog = true }
                )
            } else if (selectedVendorId != null && activeVendor != null) {
                VendorLedgerContent(
                    vendor = activeVendor,
                    purchases = shopCreditPurchases.filter { it.vendorId == activeVendor.id },
                    payments = shopCreditPayments.filter { it.vendorId == activeVendor.id },
                    isBangla = isBn,
                    useBnDigits = useBnDigits,
                    hideBalances = hideBalances,
                    currencySymbol = currSymbol,
                    onSettleClick = { settlingVendor = activeVendor },
                    onAddPurchaseClick = { showAddCreditDialog = true },
                    onArchiveVendor = { vId, archived ->
                        viewModel.archiveVendor(vId, archived)
                        selectedVendorId = null
                    }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// LENDING & BORROWING CONTENT
// -------------------------------------------------------------
@Composable
fun DebtsOverviewContent(
    contacts: List<Contact>,
    debts: List<LoanDebt>,
    totalReceivable: Double,
    totalPayable: Double,
    isBangla: Boolean,
    useBnDigits: Boolean,
    hideBalances: Boolean,
    currencySymbol: String,
    onContactClick: (Long) -> Unit
) {
    val isBn = isBangla
    val contactSummaries = remember(contacts, debts) {
        contacts.map { contact ->
            val contactDebts = debts.filter { it.contactId == contact.id || it.personName.equals(contact.name, true) }
            val owedToMe = contactDebts.filter { it.type == DebtType.OWED_TO_ME && !it.isSettled }.sumOf { it.remainingAmount }
            val iOwe = contactDebts.filter { it.type == DebtType.I_OWE && !it.isSettled }.sumOf { it.remainingAmount }
            val net = owedToMe - iOwe
            Triple(contact, net, contactDebts)
        }.sortedByDescending { kotlin.math.abs(it.second) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = IncomeGreen.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBn) "মোট পাওনা (সবাই দেবে)" else "Total Receivable",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = IncomeGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CurrencyAmountText(
                        amount = totalReceivable,
                        currencySymbol = currencySymbol,
                        useBanglaDigits = useBnDigits,
                        hideBalances = hideBalances,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = IncomeGreen
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBn) "মোট দেনা (আমি দেব)" else "Total Payable",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CurrencyAmountText(
                        amount = totalPayable,
                        currencySymbol = currencySymbol,
                        useBanglaDigits = useBnDigits,
                        hideBalances = hideBalances,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                }
            }
        }

        Text(
            text = if (isBn) "কন্টাক্টসমূহ (লেনদেন অনুসারে)" else "Contacts Ledger",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (contactSummaries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Localization.getString(Localization.Key.EMPTY_DEBTS, isBn),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(contactSummaries, key = { it.first.id }) { (contact, net, contactDebts) ->
                    val isReceivable = net > 0
                    val isPayable = net < 0
                    val badgeColor = when {
                        isReceivable -> IncomeGreen
                        isPayable -> ExpenseRed
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onContactClick(contact.id) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = badgeColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = contact.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (contact.phone.isNotBlank()) {
                                    Text(
                                        text = contact.phone,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = "${contactDebts.size} ${if (isBn) "টি এন্ট্রি" else "entries"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                CurrencyAmountText(
                                    amount = kotlin.math.abs(net),
                                    currencySymbol = currencySymbol,
                                    useBanglaDigits = useBnDigits,
                                    hideBalances = hideBalances,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = badgeColor
                                )
                                Text(
                                    text = when {
                                        isReceivable -> if (isBn) "পাবেন" else "Receivable"
                                        isPayable -> if (isBn) "দেবেন" else "Payable"
                                        else -> if (isBn) "পরিশোধিত" else "Settled"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = badgeColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactLedgerContent(
    contact: Contact,
    debts: List<LoanDebt>,
    isBangla: Boolean,
    useBnDigits: Boolean,
    hideBalances: Boolean,
    currencySymbol: String,
    onRecordPayment: (LoanDebt) -> Unit,
    onDeleteDebt: (Long) -> Unit,
    onAddEntryClick: () -> Unit
) {
    val isBn = isBangla
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (contact.phone.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = contact.phone,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Button(
                    onClick = onAddEntryClick,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBn) "নতুন এন্ট্রি" else "Add")
                }
            }
        }

        Text(
            text = if (isBn) "লেনদেনের ইতিহাস ও বাকি" else "Entries History",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (debts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Localization.getString(Localization.Key.EMPTY_DEBTS, isBn),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(debts, key = { it.id }) { debt ->
                    LoanDebtItemCard(
                        debt = debt,
                        isBangla = isBn,
                        useBnDigits = useBnDigits,
                        hideBalances = hideBalances,
                        currencySymbol = currencySymbol,
                        onRecordPayment = { onRecordPayment(debt) },
                        onDelete = { onDeleteDebt(debt.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun LoanDebtItemCard(
    debt: LoanDebt,
    isBangla: Boolean,
    useBnDigits: Boolean,
    hideBalances: Boolean,
    currencySymbol: String,
    onRecordPayment: () -> Unit,
    onDelete: () -> Unit
) {
    val isBn = isBangla
    val isOwedToMe = debt.type == DebtType.OWED_TO_ME
    val mainColor = if (isOwedToMe) IncomeGreen else ExpenseRed
    val progress = if (debt.amount > 0) (debt.paidAmount / debt.amount).toFloat().coerceIn(0f, 1f) else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = mainColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isOwedToMe) (if (isBn) "দিলাম (পাওনা)" else "Gave Money") else (if (isBn) "নিলাম (দেনা)" else "Took Money"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = mainColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (debt.isSettled) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = IncomeGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = Localization.getString(Localization.Key.SETTLE, isBn),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = if (isBn) "বাকি পরিমাণ" else "Remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    CurrencyAmountText(
                        amount = debt.remainingAmount,
                        currencySymbol = currencySymbol,
                        useBanglaDigits = useBnDigits,
                        hideBalances = hideBalances,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = mainColor
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isBn) "মূল পরিমাণ" else "Total Amount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    CurrencyAmountText(
                        amount = debt.amount,
                        currencySymbol = currencySymbol,
                        useBanglaDigits = useBnDigits,
                        hideBalances = hideBalances,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (debt.dueDate != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${if (isBn) "পরিশোধের শেষ তারিখ:" else "Due:"} ${Formatters.formatDate(debt.dueDate, isBn)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (debt.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = debt.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = mainColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            if (!debt.isSettled) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onRecordPayment,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = mainColor)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Localization.getString(Localization.Key.RECORD_PAYMENT, isBn))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SHOP CREDIT (দোকান বাকি) CONTENT & VENDOR MANAGEMENT
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopCreditOverviewContent(
    vendors: List<Vendor>,
    purchases: List<ShopCreditPurchase>,
    totalOwed: Double,
    isBangla: Boolean,
    useBnDigits: Boolean,
    hideBalances: Boolean,
    currencySymbol: String,
    onVendorClick: (Long) -> Unit,
    onAddCreditClick: () -> Unit,
    onArchiveVendor: (Long, Boolean) -> Unit
) {
    val isBn = isBangla
    var searchQuery by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var sortBy by remember { mutableStateOf(0) } // 0: Highest Balance, 1: Alphabetical, 2: Most Recent

    val activeVendors = remember(vendors) { vendors.filter { !it.isArchived } }

    val vendorSummaries = remember(activeVendors, purchases, searchQuery, selectedTag, sortBy) {
        val summaries = activeVendors.map { vendor ->
            val vPurchases = purchases.filter { it.vendorId == vendor.id }
            val owed = vPurchases.filter { !it.isSettled }.sumOf { it.remainingAmount }
            val lastActive = vPurchases.maxOfOrNull { it.dateTimestamp } ?: vendor.createdAt
            Triple(vendor, owed, lastActive)
        }

        val owedVendorsCount = summaries.count { it.second > 0 }
        val totalVendorsCount = summaries.size

        val filtered = summaries.filter { (vendor, _, _) ->
            val matchesSearch = vendor.name.contains(searchQuery, true) || vendor.phone.contains(searchQuery, true) || vendor.categoryTag.contains(searchQuery, true)
            val matchesTag = selectedTag == null || vendor.categoryTag.equals(selectedTag, true)
            matchesSearch && matchesTag
        }

        val sorted = when (sortBy) {
            0 -> filtered.sortedByDescending { it.second } // Highest Balance
            1 -> filtered.sortedBy { it.first.name } // Alphabetical
            2 -> filtered.sortedByDescending { it.third } // Most Recently Active
            else -> filtered
        }

        Triple(sorted, owedVendorsCount, totalVendorsCount)
    }

    val (displaySummaries, owedVendorsCount, totalVendorsCount) = vendorSummaries

    val availableTags = remember(activeVendors) {
        activeVendors.map { it.categoryTag }.filter { it.isNotBlank() }.distinct()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary Header with count ("owed to 4 of 9 vendors")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBn) "মোট দোকান বাকি" else "Total Shop Credit Owed",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ExpenseRed.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (isBn) "$owedVendorsCount টি দোকান / মোট $totalVendorsCount টি" else "Owed to $owedVendorsCount of $totalVendorsCount vendors",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                CurrencyAmountText(
                    amount = totalOwed,
                    currencySymbol = currencySymbol,
                    useBanglaDigits = useBnDigits,
                    hideBalances = hideBalances,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = ExpenseRed
                )
            }
        }

        // Search Bar & Sort Dropdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isBn) "দোকান খুঁজুন..." else "Search vendors...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )

            var sortExpanded by remember { mutableStateOf(false) }
            Box {
                IconButton(
                    onClick = { sortExpanded = true },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .size(50.dp)
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "Sort", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(
                    expanded = sortExpanded,
                    onDismissRequest = { sortExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (isBn) "সর্বোচ্চ বাকি (Default)" else "Highest Balance") },
                        onClick = { sortBy = 0; sortExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isBn) "নামানুসারে (A-Z)" else "Alphabetical") },
                        onClick = { sortBy = 1; sortExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isBn) "সাম্প্রতিক সক্রিয়" else "Most Recently Active") },
                        onClick = { sortBy = 2; sortExpanded = false }
                    )
                }
            }
        }

        // Optional Tag Filters Horizontally
        if (availableTags.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTag == null,
                    onClick = { selectedTag = null },
                    label = { Text(Localization.getString(Localization.Key.ALL, isBn)) }
                )
                availableTags.forEach { tag ->
                    FilterChip(
                        selected = selectedTag.equals(tag, true),
                        onClick = { selectedTag = if (selectedTag.equals(tag, true)) null else tag },
                        label = { Text(tag) }
                    )
                }
            }
        }

        // Vendors List or Empty State (Rule 9)
        if (activeVendors.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(ExpenseRed.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isBn) "কোনো দোকান বা ভেন্ডর বাকি নেই" else "No Shop Credit Records Yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isBn) "মুদি দোকান বা অন্য কোনো দোকান থেকে বাকিতে মালপত্র কিনলে এখানে হিসাব রাখুন। টাকা পরিশোধের পর হিসাব স্বয়ংক্রিয়ভাবে আপডেট হবে।" else "Track purchases taken on credit from grocery shops, pharmacies, or vendors and settle them easily.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onAddCreditClick,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBn) "দোকান / বাকি যোগ করুন" else "Add Vendor / Credit Purchase")
                    }
                }
            }
        } else if (displaySummaries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isBn) "কোনো দোকান পাওয়া যায়নি" else "No matching vendors found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(displaySummaries, key = { it.first.id }) { (vendor, owed, _) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onVendorClick(vendor.id) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(ExpenseRed.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = vendor.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (vendor.categoryTag.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                        ) {
                                            Text(
                                                text = vendor.categoryTag,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                if (vendor.phone.isNotBlank()) {
                                    Text(
                                        text = vendor.phone,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else if (vendor.locationNote.isNotBlank()) {
                                    Text(
                                        text = vendor.locationNote,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                CurrencyAmountText(
                                    amount = owed,
                                    currencySymbol = currencySymbol,
                                    useBanglaDigits = useBnDigits,
                                    hideBalances = hideBalances,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ExpenseRed
                                )
                                if (owed == 0.0) {
                                    TextButton(
                                        onClick = { onArchiveVendor(vendor.id, true) },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = if (isBn) "আর্কাইভ" else "Archive",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    Text(
                                        text = if (isBn) "বাকি আছে" else "Owed",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ExpenseRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VendorLedgerContent(
    vendor: Vendor,
    purchases: List<ShopCreditPurchase>,
    payments: List<ShopCreditPayment>,
    isBangla: Boolean,
    useBnDigits: Boolean,
    hideBalances: Boolean,
    currencySymbol: String,
    onSettleClick: () -> Unit,
    onAddPurchaseClick: () -> Unit,
    onArchiveVendor: (Long, Boolean) -> Unit
) {
    val context = LocalContext.current
    val isBn = isBangla
    val totalOwed = purchases.filter { !it.isSettled }.sumOf { it.remainingAmount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = vendor.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (vendor.categoryTag.isNotBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = vendor.categoryTag,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        if (vendor.phone.isNotBlank()) {
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${vendor.phone}"))
                                        context.startActivity(intent)
                                    }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = vendor.phone,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (vendor.locationNote.isNotBlank()) {
                            Text(
                                text = vendor.locationNote,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (totalOwed == 0.0) {
                        IconButton(onClick = { onArchiveVendor(vendor.id, true) }) {
                            Icon(Icons.Default.Archive, contentDescription = "Archive Vendor", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onAddPurchaseClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBn) "বাকিতে ক্রয়" else "Add Purchase")
                    }

                    if (totalOwed > 0) {
                        Button(
                            onClick = onSettleClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBn) "বাকি পরিশোধ" else "Pay Full")
                        }
                    }
                }
            }
        }

        Text(
            text = if (isBn) "বাকিতে ক্রয়ের ইতিহাস ও পেমেন্ট" else "Purchase & Payment Timeline",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            item {
                Text(
                    text = if (isBn) "ক্রয়সমূহ (Purchases)" else "Purchases",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            items(purchases, key = { "p_${it.id}" }) { purchase ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = purchase.note.ifBlank { if (isBn) "বাকিতে ক্রয়" else "Credit Purchase" },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            CurrencyAmountText(
                                amount = purchase.amount,
                                currencySymbol = currencySymbol,
                                useBanglaDigits = useBnDigits,
                                hideBalances = hideBalances,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = Formatters.formatDate(purchase.dateTimestamp, isBn),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${if (isBn) "বাকি:" else "Remaining:"} ${purchase.remainingAmount}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (purchase.isSettled) IncomeGreen else ExpenseRed
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isBn) "পরিশোধের ইতিহাস (Payments)" else "Payment History",
                    style = MaterialTheme.typography.labelLarge,
                    color = IncomeGreen,
                    fontWeight = FontWeight.Bold
                )
            }
            if (payments.isEmpty()) {
                item {
                    Text(
                        text = if (isBn) "কোনো পেমেন্ট করা হয়নি" else "No payments recorded yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(payments, key = { "pay_${it.id}" }) { payment ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = IncomeGreen.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = payment.note.ifBlank { if (isBn) "বাকি পরিশোধ" else "Credit Payment" },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                CurrencyAmountText(
                                    amount = payment.amount,
                                    currencySymbol = currencySymbol,
                                    useBanglaDigits = useBnDigits,
                                    hideBalances = hideBalances,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Formatters.formatDate(payment.dateTimestamp, isBn),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DIALOGS
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShopCreditDialog(
    vendors: List<Vendor>,
    purchases: List<ShopCreditPurchase>,
    initialVendorId: Long?,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (vendorId: Long, vendorName: String, amount: Double, dueDate: Long?, note: String, phone: String, locationNote: String, categoryTag: String) -> Unit
) {
    val isBn = isBangla
    val activeVendors = remember(vendors) { vendors.filter { !it.isArchived } }

    val recentVendors = remember(activeVendors, purchases) {
        activeVendors.sortedByDescending { v ->
            purchases.filter { it.vendorId == v.id }.maxOfOrNull { it.dateTimestamp } ?: v.createdAt
        }.take(5)
    }

    var selectedVendor by remember { mutableStateOf(activeVendors.find { it.id == initialVendorId }) }
    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var locationNote by remember { mutableStateOf("") }
    var categoryTag by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var isAddingNewVendor by remember { mutableStateOf(activeVendors.isEmpty() || initialVendorId == null) }

    val duplicateSuggestion = remember(newName, activeVendors) {
        val trimmed = newName.trim()
        if (trimmed.length >= 2) {
            activeVendors.firstOrNull { v ->
                !v.name.equals(trimmed, true) && (v.name.contains(trimmed, true) || trimmed.contains(v.name, true) ||
                        (v.name.take(3).equals(trimmed.take(3), true)))
            }
        } else null
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (isBn) "বাকিতে ক্রয় (Shop Credit Purchase)" else "Add Credit Purchase",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (recentVendors.isNotEmpty() && !isAddingNewVendor) {
                    Text(
                        text = if (isBn) "সম্প্রতি ব্যবহৃত দোকান:" else "Frequent Shops:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        recentVendors.forEach { vendor ->
                            FilterChip(
                                selected = selectedVendor?.id == vendor.id,
                                onClick = { selectedVendor = vendor },
                                label = { Text(vendor.name) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (!isAddingNewVendor) {
                    var vendorExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = vendorExpanded,
                        onExpandedChange = { vendorExpanded = !vendorExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedVendor?.name ?: (if (isBn) "অন্য দোকান বেছে নিন..." else "Search or select vendor..."),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isBn) "দোকান / ভেন্ডর" else "Vendor / Shop") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vendorExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = vendorExpanded,
                            onDismissRequest = { vendorExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isBn) "+ নতুন দোকান যোগ করুন" else "+ Add New Vendor") },
                                onClick = {
                                    isAddingNewVendor = true
                                    selectedVendor = null
                                    vendorExpanded = false
                                }
                            )
                            activeVendors.forEach { vendor ->
                                DropdownMenuItem(
                                    text = { Text(vendor.name) },
                                    onClick = {
                                        selectedVendor = vendor
                                        vendorExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text(if (isBn) "দোকানের নাম" else "Shop / Vendor Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (duplicateSuggestion != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedVendor = duplicateSuggestion
                                    isAddingNewVendor = false
                                    newName = ""
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBn) "আপনি কি '${duplicateSuggestion.name}' বুঝাতে চেয়েছেন? ট্যাপ করে সিলেক্ট করুন।" else "Did you mean '${duplicateSuggestion.name}'? Tap to use.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text(if (isBn) "ফোন নম্বর (ঐচ্ছিক)" else "Phone (Optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = categoryTag,
                        onValueChange = { categoryTag = it },
                        label = { Text(if (isBn) "ক্যাটেগরি ট্যাগ (যেমন: মুদি, ফার্মেসী)" else "Category Tag (e.g. Grocery, Pharmacy)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (activeVendors.isNotEmpty()) {
                        TextButton(onClick = { isAddingNewVendor = false }) {
                            Text(if (isBn) "বিদ্যমান তালিকা থেকে বেছে নিন" else "Choose from existing vendors")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(Localization.getString(Localization.Key.AMOUNT, isBn)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isBn) "আইটেম / বিবরণ (যেমন: চাল, ডাল)" else "Item / Description (e.g. rice, oil)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(Localization.getString(Localization.Key.CANCEL, isBn))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                val vId = selectedVendor?.id ?: 0L
                                val vName = selectedVendor?.name ?: newName.trim()
                                val vPhone = selectedVendor?.phone ?: newPhone.trim()
                                val vLoc = selectedVendor?.locationNote ?: locationNote.trim()
                                val vTag = selectedVendor?.categoryTag ?: categoryTag.trim()
                                if (vName.isNotBlank()) {
                                    onSave(vId, vName, amt, null, note.trim(), vPhone, vLoc, vTag)
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(Localization.getString(Localization.Key.SAVE, isBn))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentDialog(
    debt: LoanDebt,
    accounts: List<com.plusemon.hisab.ui.viewmodel.AccountWithBalance>,
    isBangla: Boolean,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (accountId: Long, paymentAmount: Double, note: String) -> Unit
) {
    val isBn = isBangla
    var amountText by remember { mutableStateOf(debt.remainingAmount.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }) }
    var note by remember { mutableStateOf("") }
    var selectedAccount by remember { mutableStateOf(accounts.firstOrNull()?.account) }
    var accountExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "${Localization.getString(Localization.Key.RECORD_PAYMENT, isBn)} - ${debt.personName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                ExposedDropdownMenuBox(
                    expanded = accountExpanded,
                    onExpandedChange = { accountExpanded = !accountExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedAccount?.name ?: (if (isBn) "অ্যাকাউন্ট বেছে নিন" else "Select Account"),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(Localization.getString(Localization.Key.ACCOUNT, isBn)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = accountExpanded,
                        onDismissRequest = { accountExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.account.name) },
                                onClick = {
                                    selectedAccount = acc.account
                                    accountExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(Localization.getString(Localization.Key.AMOUNT, isBn)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(Localization.getString(Localization.Key.NOTE, isBn)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(Localization.getString(Localization.Key.CANCEL, isBn))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            val accId = selectedAccount?.id ?: 1L
                            if (amt > 0) {
                                onConfirm(accId, amt, note.trim())
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(Localization.getString(Localization.Key.SAVE, isBn))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLoanEntryDialog(
    contacts: List<Contact>,
    accounts: List<com.plusemon.hisab.ui.viewmodel.AccountWithBalance>,
    initialContactId: Long?,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (contactId: Long, personName: String, accountId: Long, amount: Double, type: DebtType, dueDate: Long?, note: String, phone: String) -> Unit
) {
    val isBn = isBangla
    var selectedType by remember { mutableStateOf(DebtType.OWED_TO_ME) }
    var selectedContact by remember { mutableStateOf(contacts.find { it.id == initialContactId }) }
    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedAccount by remember { mutableStateOf(accounts.firstOrNull()?.account) }

    var isAddingNewContact by remember { mutableStateOf(contacts.isEmpty() || initialContactId == null) }
    var accountExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (isBn) "ধার / দেনা-পাওনা এন্ট্রি" else "Add Loan Entry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedType = DebtType.OWED_TO_ME },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == DebtType.OWED_TO_ME) IncomeGreen else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedType == DebtType.OWED_TO_ME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isBn) "দিলাম (পাওনা)" else "Gave Money", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { selectedType = DebtType.I_OWE },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == DebtType.I_OWE) ExpenseRed else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedType == DebtType.I_OWE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isBn) "নিলাম (দেনা)" else "Took Money", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!isAddingNewContact && contacts.isNotEmpty()) {
                    var contactExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = contactExpanded,
                        onExpandedChange = { contactExpanded = !contactExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedContact?.name ?: (if (isBn) "কন্টাক্ট বেছে নিন" else "Select Contact"),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(Localization.getString(Localization.Key.PERSON_NAME, isBn)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = contactExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = contactExpanded,
                            onDismissRequest = { contactExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isBn) "+ নতুন কন্টাক্ট যোগ করুন" else "+ Add New Contact") },
                                onClick = {
                                    isAddingNewContact = true
                                    selectedContact = null
                                    contactExpanded = false
                                }
                            )
                            contacts.forEach { contact ->
                                DropdownMenuItem(
                                    text = { Text(contact.name) },
                                    onClick = {
                                        selectedContact = contact
                                        contactExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text(if (isBn) "ব্যক্তির নাম" else "Person Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text(if (isBn) "ফোন নম্বর (ঐচ্ছিক)" else "Phone (Optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (contacts.isNotEmpty()) {
                        TextButton(onClick = { isAddingNewContact = false }) {
                            Text(if (isBn) "তালিকা থেকে কন্টাক্ট বেছে নিন" else "Select from existing contacts")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = accountExpanded,
                    onExpandedChange = { accountExpanded = !accountExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedAccount?.name ?: (if (isBn) "অ্যাকাউন্ট বেছে নিন" else "Select Account"),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(Localization.getString(Localization.Key.ACCOUNT, isBn)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = accountExpanded,
                        onDismissRequest = { accountExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.account.name) },
                                onClick = {
                                    selectedAccount = acc.account
                                    accountExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(Localization.getString(Localization.Key.AMOUNT, isBn)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(Localization.getString(Localization.Key.NOTE, isBn)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(Localization.getString(Localization.Key.CANCEL, isBn))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            val accId = selectedAccount?.id ?: 1L
                            if (amt > 0) {
                                val cId = selectedContact?.id ?: 0L
                                val pName = selectedContact?.name ?: newName.trim()
                                val pPhone = selectedContact?.phone ?: newPhone.trim()
                                if (pName.isNotBlank()) {
                                    onSave(cId, pName, accId, amt, selectedType, null, note.trim(), pPhone)
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(Localization.getString(Localization.Key.SAVE, isBn))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettleShopCreditDialog(
    vendor: Vendor,
    defaultAmount: Double,
    accounts: List<com.plusemon.hisab.ui.viewmodel.AccountWithBalance>,
    isBangla: Boolean,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (accountId: Long, amount: Double, note: String) -> Unit
) {
    val isBn = isBangla
    var amountText by remember { mutableStateOf(defaultAmount.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }) }
    var note by remember { mutableStateOf("") }
    var selectedAccount by remember { mutableStateOf(accounts.firstOrNull()?.account) }
    var accountExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "${if (isBn) "বাকি পরিশোধ (Settle)" else "Settle Shop Credit"} - ${vendor.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                ExposedDropdownMenuBox(
                    expanded = accountExpanded,
                    onExpandedChange = { accountExpanded = !accountExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedAccount?.name ?: (if (isBn) "অ্যাকাউন্ট বেছে নিন" else "Select Account"),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(Localization.getString(Localization.Key.ACCOUNT, isBn)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = accountExpanded,
                        onDismissRequest = { accountExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.account.name) },
                                onClick = {
                                    selectedAccount = acc.account
                                    accountExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(Localization.getString(Localization.Key.AMOUNT, isBn)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(Localization.getString(Localization.Key.NOTE, isBn)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(Localization.getString(Localization.Key.CANCEL, isBn))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            val accId = selectedAccount?.id ?: 1L
                            if (amt > 0) {
                                onConfirm(accId, amt, note.trim())
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(Localization.getString(Localization.Key.SAVE, isBn))
                    }
                }
            }
        }
    }
}
