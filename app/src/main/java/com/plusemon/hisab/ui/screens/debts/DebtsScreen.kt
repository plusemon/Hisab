package com.plusemon.hisab.ui.screens.debts

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
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
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import com.plusemon.hisab.domain.util.ContactPhoneOption
import com.plusemon.hisab.domain.util.ContactUtils
import com.plusemon.hisab.domain.util.SelectContactPhoneDialog
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
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
import com.plusemon.hisab.ui.components.DeleteConfirmationDialog
import com.plusemon.hisab.ui.components.EmptyStateView
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

    val debtPagerState = rememberPagerState(initialPage = 0) { 2 }
    val coroutineScope = rememberCoroutineScope()

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
    var showAddVendorDialog by remember { mutableStateOf(false) }
    var settlingVendor by remember { mutableStateOf<Vendor?>(null) }
    var debtToDelete by remember { mutableStateOf<LoanDebt?>(null) }

    if (debtToDelete != null) {
        val target = debtToDelete!!
        val amountStr = Formatters.formatAmount(target.remainingAmount, currSymbol, useBnDigits)
        val personOrNote = target.personName + (if (target.note.isNotBlank()) " • " + target.note else "")
        val typeLabel = if (target.type == DebtType.OWED_TO_ME) (if (isBn) "পাওনা" else "Receivable") else (if (isBn) "দেনা" else "Payable")
        DeleteConfirmationDialog(
            title = if (isBn) "ধার/দেনার হিসাব মুছে ফেলবেন?" else "Delete Loan/Debt Entry?",
            message = if (isBn)
                "আপনি কি নিশ্চিত যে এই ধার/দেনার এন্ট্রিটি মুছে ফেলতে চান?"
            else
                "Are you sure you want to delete this loan/debt record?",
            itemDetail = "$personOrNote • $typeLabel: $amountStr",
            isBangla = isBn,
            onConfirm = {
                viewModel.deleteLoanDebt(target.id)
                debtToDelete = null
            },
            onDismiss = {
                debtToDelete = null
            }
        )
    }

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
                viewModel.addLoanDebt(contactId ?: 0L, personName, accountId, amount, type, dueDate, note, phone)
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

    if (showAddVendorDialog) {
        AddNewVendorDialog(
            isBangla = isBn,
            onDismiss = { showAddVendorDialog = false },
            onSave = { name, phone, locationNote, tag ->
                viewModel.addVendor(name, phone, locationNote, tag)
                showAddVendorDialog = false
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
                viewModel.addShopCreditPurchase(vendorId ?: 0L, vendorName, 1L, amount, dueDate, note, phone, locationNote, tag)
                showAddCreditDialog = false
            },
            onAddNewVendor = { name, phone, locationNote, tag, onCreated ->
                viewModel.addVendor(name, phone, locationNote, tag) { newVendor ->
                    onCreated(newVendor)
                }
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

    BackHandler(enabled = selectedContactId != null || selectedVendorId != null) {
        selectedContactId = null
        selectedVendorId = null
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (selectedContactId != null || selectedVendorId != null) {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Text(
                            text = when {
                                selectedContactId != null && activeContact != null -> activeContact.name
                                selectedVendorId != null && activeVendor != null -> activeVendor.name
                                else -> ""
                            },
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            selectedContactId = null
                            selectedVendorId = null
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (selectedContactId == null && selectedVendorId == null) {
                FloatingActionButton(
                    onClick = {
                        if (debtPagerState.currentPage == 0) showAddLoanDialog = true else showAddCreditDialog = true
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
                    selectedTabIndex = debtPagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = debtPagerState.currentPage == 0,
                        onClick = {
                            coroutineScope.launch {
                                debtPagerState.animateScrollToPage(0)
                            }
                        },
                        text = {
                            Text(
                                text = if (isBn) "ধার ও ঋণ" else "Lending & Borrowing",
                                fontWeight = if (debtPagerState.currentPage == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("tab_lending")
                    )
                    Tab(
                        selected = debtPagerState.currentPage == 1,
                        onClick = {
                            coroutineScope.launch {
                                debtPagerState.animateScrollToPage(1)
                            }
                        },
                        text = {
                            Text(
                                text = if (isBn) "দোকান বাকি" else "Shop Credit",
                                fontWeight = if (debtPagerState.currentPage == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("tab_shop_credit")
                    )
                }

                HorizontalPager(
                    state = debtPagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    if (page == 0) {
                        DebtsOverviewContent(
                            contacts = contacts,
                            debts = allDebts,
                            totalReceivable = totalOwedToMe,
                            totalPayable = totalIOwe,
                            isBangla = isBn,
                            useBnDigits = useBnDigits,
                            hideBalances = hideBalances,
                            currencySymbol = currSymbol,
                            onContactClick = { contactId -> selectedContactId = contactId },
                            onAddDebtClick = { showAddLoanDialog = true }
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
                            onAddNewVendorClick = { showAddVendorDialog = true },
                            onArchiveVendor = { vId, archived -> viewModel.archiveVendor(vId, archived) }
                        )
                    }
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
                    onDeleteDebt = { debt -> debtToDelete = debt },
                    onAddEntryClick = { showAddLoanDialog = true },
                    onUpdateContactPhone = { updatedContact, newPhone ->
                        viewModel.updateContact(updatedContact.copy(phone = newPhone))
                    }
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
// LENDING & BORROWING CONTENT (FLAT DESIGN SYSTEM)
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
    onContactClick: (Long) -> Unit,
    onAddDebtClick: () -> Unit
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
        // Flat cards for Receivable & Payable (No soft pastel fills)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, IncomeGreen.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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

        // Rule 7 / Prompt requirement: Empty state consistent with Shop Credit (Icon, headline, description, CTA)
        if (contactSummaries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    icon = Icons.Default.People,
                    title = Localization.getString(Localization.Key.EMPTY_DEBTS, isBn),
                    description = if (isBn) "কারো কাছে টাকা পাওনা বা দেনা থাকলে এখানে হিসাব রাখতে পারেন।" else "Track money owed to you or money you owe to others here.",
                    actionLabel = if (isBn) "+ নতুন ধার যোগ করুন" else "+ Record Loan/Debt",
                    onActionClick = onAddDebtClick
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
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = badgeColor,
                                    modifier = Modifier.size(22.dp)
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

                            if (contact.phone.isNotBlank()) {
                                val context = LocalContext.current
                                IconButton(
                                    onClick = { ContactUtils.dialPhoneNumber(context, contact.phone, isBn) },
                                    modifier = Modifier.size(36.dp).testTag("quick_call_${contact.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = if (isBn) "কল করুন" else "Call",
                                        tint = IncomeGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                CurrencyAmountText(
                                    amount = kotlin.math.abs(net),
                                    currencySymbol = currencySymbol,
                                    useBanglaDigits = useBnDigits,
                                    hideBalances = hideBalances,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
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
fun EditContactPhoneDialog(
    contact: Contact,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val context = LocalContext.current
    var phone by remember { mutableStateOf(contact.phone) }
    var phoneOptionsForSelection by remember { mutableStateOf<List<ContactPhoneOption>?>(null) }
    var pendingContactName by remember { mutableStateOf("") }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        if (uri != null) {
            val result = ContactUtils.extractContact(context, uri)
            if (result != null) {
                when {
                    result.phones.isEmpty() -> {
                        Toast.makeText(
                            context,
                            if (isBangla) "এই কন্টাক্টে কোনো ফোন নম্বর পাওয়া যায়নি" else "No phone numbers found in this contact",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    result.phones.size == 1 -> {
                        phone = result.phones.first().rawNumber
                    }
                    else -> {
                        pendingContactName = result.name
                        phoneOptionsForSelection = result.phones
                    }
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        contactPickerLauncher.launch(null)
    }

    val onPickContact = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        } else {
            contactPickerLauncher.launch(null)
        }
    }

    if (phoneOptionsForSelection != null) {
        SelectContactPhoneDialog(
            contactName = pendingContactName,
            options = phoneOptionsForSelection!!,
            isBangla = isBangla,
            onSelectPhone = { selected ->
                phone = selected.rawNumber
                phoneOptionsForSelection = null
            },
            onDismiss = {
                phoneOptionsForSelection = null
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(
                    text = if (isBangla) "ফোন নম্বর পরিবর্তন / যুক্ত করুন" else "Update Phone Number",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onPickContact,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ContactPhone, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isBangla) "ফোনের কন্টাক্ট থেকে চুজ করুন" else "Pick from Phone Contacts")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (isBangla) "মোবাইল নম্বর" else "Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
                        Text(if (isBangla) "বাতিল" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(phone.trim())
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isBangla) "সংরক্ষণ" else "Save")
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
    onDeleteDebt: (LoanDebt) -> Unit,
    onAddEntryClick: () -> Unit,
    onUpdateContactPhone: ((Contact, String) -> Unit)? = null
) {
    val isBn = isBangla
    val context = LocalContext.current
    var showEditPhoneDialog by remember { mutableStateOf(false) }

    val phoneToCall = contact.phone.ifBlank { debts.firstOrNull { it.phone.isNotBlank() }?.phone ?: "" }

    if (showEditPhoneDialog) {
        EditContactPhoneDialog(
            contact = contact,
            isBangla = isBn,
            onDismiss = { showEditPhoneDialog = false },
            onSave = { newPhone ->
                onUpdateContactPhone?.invoke(contact, newPhone)
                showEditPhoneDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
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
                        if (phoneToCall.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { ContactUtils.dialPhoneNumber(context, phoneToCall, isBn) }
                                    .padding(vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = IncomeGreen
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = phoneToCall,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = IncomeGreen
                                )
                            }
                        } else {
                            Text(
                                text = if (isBn) "কোনো ফোন নম্বর যুক্ত নেই" else "No phone number linked",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (onUpdateContactPhone != null) {
                        IconButton(
                            onClick = { showEditPhoneDialog = true },
                            modifier = Modifier.size(36.dp).testTag("edit_contact_phone_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = if (isBn) "নম্বর এডিট করুন" else "Edit Phone",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons on Details page: Call and Add Entry
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (phoneToCall.isNotBlank()) {
                        Button(
                            onClick = { ContactUtils.dialPhoneNumber(context, phoneToCall, isBn) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IncomeGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("contact_details_call_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = if (isBn) "কল করুন" else "Call",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBn) "কল করুন" else "Call", fontWeight = FontWeight.Bold)
                        }
                    } else if (onUpdateContactPhone != null) {
                        OutlinedButton(
                            onClick = { showEditPhoneDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("contact_details_add_phone_btn")
                        ) {
                            Icon(Icons.Default.ContactPhone, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBn) "নম্বর যুক্ত করুন" else "Add Phone", fontWeight = FontWeight.Medium)
                        }
                    }

                    Button(
                        onClick = onAddEntryClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("contact_details_add_entry_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBn) "নতুন এন্ট্রি" else "Add Entry", fontWeight = FontWeight.SemiBold)
                    }
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
                EmptyStateView(
                    icon = Icons.Default.People,
                    title = Localization.getString(Localization.Key.EMPTY_DEBTS, isBn),
                    description = if (isBn) "এই কন্টাক্টের কোনো হিসাব পাওয়া যায়নি।" else "No debt or loan records found for this contact.",
                    actionLabel = if (isBn) "+ এন্ট্রি যোগ করুন" else "+ Add Entry",
                    onActionClick = onAddEntryClick
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
                        onDelete = { onDeleteDebt(debt) }
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    color = MaterialTheme.colorScheme.surfaceVariant
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
                        fontWeight = FontWeight.Bold,
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
                        style = MaterialTheme.typography.titleMedium,
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
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = mainColor)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Localization.getString(Localization.Key.RECORD_PAYMENT, isBn), fontWeight = FontWeight.Bold)
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
    onAddNewVendorClick: () -> Unit = {},
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
        // Summary Header Card (Flat card with border, no soft red fill)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
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
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRed
                )
            }
        }

        // Section Title & Add New Shop Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isBn) "দোকানের তালিকা" else "Shop Directory",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onAddNewVendorClick,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isBn) "নতুন শপ" else "Add Shop", fontWeight = FontWeight.Bold)
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
                        text = { Text(if (isBn) "সর্বোচ্চ বাকি" else "Highest Balance") },
                        onClick = { sortBy = 0; sortExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isBn) "নামানুসারে" else "Alphabetical") },
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

        // Vendors List or Empty State using EmptyStateView
        if (activeVendors.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    icon = Icons.Default.Store,
                    title = if (isBn) "কোনো দোকান বাকি নেই" else "No shop credit recorded",
                    description = if (isBn) "মুদির দোকান, ফার্মেসি বা অন্যান্য দোকানের বাকি হিসাব রাখুন।" else "Keep track of credit purchases from local grocery, pharmacy, or retail stores.",
                    actionLabel = if (isBn) "+ দোকান বাকি যোগ করুন" else "+ Add Shop Credit",
                    onActionClick = onAddCreditClick
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(displaySummaries, key = { it.first.id }) { (vendor, owedAmount, lastActive) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onVendorClick(vendor.id) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    tint = if (owedAmount > 0) ExpenseRed else IncomeGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = vendor.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (vendor.categoryTag.isNotBlank()) vendor.categoryTag else if (isBn) "সাধারণ দোকান" else "General Store",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                CurrencyAmountText(
                                    amount = owedAmount,
                                    currencySymbol = currencySymbol,
                                    useBanglaDigits = useBnDigits,
                                    hideBalances = hideBalances,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (owedAmount > 0) ExpenseRed else IncomeGreen
                                )
                                Text(
                                    text = if (owedAmount > 0) (if (isBn) "বাকি আছে" else "Owed") else (if (isBn) "পরিশোধিত" else "Settled"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (owedAmount > 0) ExpenseRed else IncomeGreen,
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
    val isBn = isBangla
    val totalRemaining = purchases.filter { !it.isSettled }.sumOf { it.remainingAmount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = vendor.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (vendor.phone.isNotBlank()) {
                            val context = LocalContext.current
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { ContactUtils.dialPhoneNumber(context, vendor.phone, isBn) }
                                    .padding(vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = IncomeGreen
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = vendor.phone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = IncomeGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (vendor.phone.isNotBlank()) {
                        val context = LocalContext.current
                        IconButton(
                            onClick = { ContactUtils.dialPhoneNumber(context, vendor.phone, isBn) },
                            modifier = Modifier.testTag("call_vendor_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = if (isBn) "কল করুন" else "Call",
                                tint = IncomeGreen
                            )
                        }
                    }

                    IconButton(onClick = { onArchiveVendor(vendor.id, true) }) {
                        Icon(Icons.Default.Archive, contentDescription = "Archive", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = if (isBn) "মোট বাকি" else "Total Owed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        CurrencyAmountText(
                            amount = totalRemaining,
                            currencySymbol = currencySymbol,
                            useBanglaDigits = useBnDigits,
                            hideBalances = hideBalances,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onAddPurchaseClick,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBn) "নতুন বাকি" else "Add")
                        }

                        if (totalRemaining > 0) {
                            Button(
                                onClick = onSettleClick,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
                            ) {
                                Text(if (isBn) "বাকি শোধ" else "Settle")
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = if (isBn) "ক্রয় ও পেমেন্ট ইতিহাস" else "History Ledger",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (purchases.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    icon = Icons.Default.Store,
                    title = if (isBn) "কোনো ক্রয়ের ইতিহাস নেই" else "No purchases recorded",
                    description = if (isBn) "এই দোকানের নতুন বাকি বা কেনাকাটা যোগ করুন।" else "No shop credit purchases found for this vendor.",
                    actionLabel = if (isBn) "+ নতুন বাকি যোগ করুন" else "+ Add Purchase",
                    onActionClick = onAddPurchaseClick
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(purchases, key = { it.id }) { purchase ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = Formatters.formatDate(purchase.dateTimestamp, isBn),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (purchase.isSettled) {
                                    Text(
                                        text = if (isBn) "পরিশোধিত" else "Settled",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = IncomeGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (purchase.note.isNotBlank()) purchase.note else if (isBn) "বাকি পণ্য ক্রয়" else "Credit Purchase",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                CurrencyAmountText(
                                    amount = purchase.amount,
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
                }
            }
        }
    }
}

// Dialogs remain functional
@Composable
fun AddLoanEntryDialog(
    contacts: List<Contact>,
    accounts: List<com.plusemon.hisab.ui.viewmodel.AccountWithBalance>,
    initialContactId: Long?,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (contactId: Long?, personName: String, accountId: Long, amount: Double, type: DebtType, dueDate: Long?, note: String, phone: String) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(contacts.find { it.id == initialContactId }?.name ?: "") }
    var phone by remember { mutableStateOf(contacts.find { it.id == initialContactId }?.phone ?: "") }
    var amountText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(DebtType.OWED_TO_ME) } // OWED_TO_ME = Gave, I_OWE = Took
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.account?.id ?: 0L) }
    var note by remember { mutableStateOf("") }

    var phoneOptionsForSelection by remember { mutableStateOf<List<ContactPhoneOption>?>(null) }
    var pendingContactName by remember { mutableStateOf("") }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        if (uri != null) {
            val result = ContactUtils.extractContact(context, uri)
            if (result != null) {
                if (result.name.isNotBlank()) {
                    name = result.name
                }
                when {
                    result.phones.isEmpty() -> {
                        Toast.makeText(
                            context,
                            if (isBangla) "এই কন্টাক্টে কোনো ফোন নম্বর পাওয়া যায়নি" else "No phone numbers found in this contact",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    result.phones.size == 1 -> {
                        phone = result.phones.first().rawNumber
                    }
                    else -> {
                        pendingContactName = result.name
                        phoneOptionsForSelection = result.phones
                    }
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        contactPickerLauncher.launch(null)
    }

    val onPickContactClick = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        } else {
            contactPickerLauncher.launch(null)
        }
    }

    if (phoneOptionsForSelection != null) {
        SelectContactPhoneDialog(
            contactName = pendingContactName,
            options = phoneOptionsForSelection!!,
            isBangla = isBangla,
            onSelectPhone = { selected ->
                phone = selected.rawNumber
                phoneOptionsForSelection = null
            },
            onDismiss = {
                phoneOptionsForSelection = null
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(
                    text = if (isBangla) "ধার / দেনা এন্ট্রি" else "Record Loan / Debt",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Type Toggle
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
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isBangla) "টাকা দিলাম (পাবো)" else "Gave Money", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { selectedType = DebtType.I_OWE },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == DebtType.I_OWE) ExpenseRed else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedType == DebtType.I_OWE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isBangla) "টাকা নিলাম (দেবো)" else "Took Money", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pick from Contacts Button
                OutlinedButton(
                    onClick = onPickContactClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pick_contact_btn"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ContactPhone,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBangla) "ফোনের কন্টাক্ট থেকে চুজ করুন" else "Pick from Phone Contacts",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isBangla) "ব্যক্তির নাম" else "Person Name") },
                    trailingIcon = {
                        IconButton(
                            onClick = onPickContactClick,
                            modifier = Modifier.testTag("pick_contact_name_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContactPhone,
                                contentDescription = if (isBangla) "কন্টাক্ট থেকে চুজ করুন" else "Pick from Contacts",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("person_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (isBangla) "মোবাইল নম্বর (ঐচ্ছিক)" else "Phone Number (Optional)") },
                    trailingIcon = {
                        IconButton(
                            onClick = onPickContactClick,
                            modifier = Modifier.testTag("pick_contact_phone_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Contacts,
                                contentDescription = if (isBangla) "কন্টাক্ট থেকে চুজ করুন" else "Pick from Contacts",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("person_phone_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(Localization.getString(Localization.Key.AMOUNT, isBangla)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("amount_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(Localization.getString(Localization.Key.NOTE, isBangla)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("note_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(Localization.getString(Localization.Key.CANCEL, isBangla))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank() && amt > 0) {
                                val matched = contacts.find { it.name.equals(name.trim(), true) }
                                onSave(matched?.id, name.trim(), selectedAccountId, amt, selectedType, null, note.trim(), phone.trim())
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_debt_button")
                    ) {
                        Text(Localization.getString(Localization.Key.SAVE, isBangla), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RecordPaymentDialog(
    debt: LoanDebt,
    accounts: List<com.plusemon.hisab.ui.viewmodel.AccountWithBalance>,
    isBangla: Boolean,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (accountId: Long, amount: Double, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf(debt.remainingAmount.toString()) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.account?.id ?: 0L) }
    var note by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(
                    text = Localization.getString(Localization.Key.RECORD_PAYMENT, isBangla),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(Localization.getString(Localization.Key.AMOUNT, isBangla)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(Localization.getString(Localization.Key.NOTE, isBangla)) },
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
                        Text(Localization.getString(Localization.Key.CANCEL, isBangla))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                onConfirm(selectedAccountId, amt, note.trim())
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(Localization.getString(Localization.Key.SAVE, isBangla), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShopCreditDialog(
    vendors: List<Vendor>,
    purchases: List<ShopCreditPurchase>,
    initialVendorId: Long?,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (vendorId: Long?, vendorName: String, amount: Double, dueDate: Long?, note: String, phone: String, locationNote: String, tag: String) -> Unit,
    onAddNewVendor: ((name: String, phone: String, locationNote: String, categoryTag: String, onCreated: (Vendor) -> Unit) -> Unit)? = null
) {
    val activeVendors = remember(vendors) { vendors.filter { !it.isArchived } }
    var selectedVendor by remember(vendors, initialVendorId) {
        mutableStateOf(activeVendors.find { it.id == initialVendorId })
    }
    var vendorSearchQuery by remember {
        mutableStateOf(selectedVendor?.name ?: "")
    }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(selectedVendor?.phone ?: "") }
    var locationNote by remember { mutableStateOf(selectedVendor?.locationNote ?: "") }
    var categoryTag by remember { mutableStateOf(selectedVendor?.categoryTag ?: "General") }

    var dropdownExpanded by remember { mutableStateOf(false) }
    var showCreateVendorDialog by remember { mutableStateOf(false) }

    if (showCreateVendorDialog) {
        AddNewVendorDialog(
            initialName = if (selectedVendor == null) vendorSearchQuery.trim() else "",
            isBangla = isBangla,
            onDismiss = { showCreateVendorDialog = false },
            onSave = { vName, vPhone, vLocation, vTag ->
                if (onAddNewVendor != null) {
                    onAddNewVendor(vName, vPhone, vLocation, vTag) { newVendor ->
                        selectedVendor = newVendor
                        vendorSearchQuery = newVendor.name
                        phone = newVendor.phone
                        locationNote = newVendor.locationNote
                        categoryTag = newVendor.categoryTag
                    }
                } else {
                    vendorSearchQuery = vName
                    phone = vPhone
                    locationNote = vLocation
                    categoryTag = vTag
                }
                showCreateVendorDialog = false
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isBangla) "দোকান বাকি রেকর্ড করুন" else "Add Shop Credit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section Title & Add New Shop Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "শপ / দোকান নির্বাচন করুন *" else "Select Store / Shop *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(
                        onClick = { showCreateVendorDialog = true },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = if (isBangla) "নতুন শপ" else "New Shop",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Selected Vendor Card or Selector
                if (selectedVendor != null) {
                    val vendor = selectedVendor!!
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = vendor.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (vendor.categoryTag.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = vendor.categoryTag,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                val subText = listOf(vendor.phone, vendor.locationNote).filter { it.isNotBlank() }.joinToString(" • ")
                                if (subText.isNotBlank()) {
                                    Text(
                                        text = subText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    selectedVendor = null
                                    vendorSearchQuery = ""
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove selected shop",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Quick-selection chips from existing shops
                    if (activeVendors.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            activeVendors.take(5).forEach { vendor ->
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        selectedVendor = vendor
                                        vendorSearchQuery = vendor.name
                                        phone = vendor.phone
                                        locationNote = vendor.locationNote
                                        categoryTag = vendor.categoryTag
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(16.dp))
                                    },
                                    label = {
                                        Text(vendor.name, maxLines = 1)
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Dropdown / Search Input
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = it }
                    ) {
                        val filteredVendors = remember(activeVendors, vendorSearchQuery) {
                            if (vendorSearchQuery.isBlank()) activeVendors
                            else activeVendors.filter {
                                it.name.contains(vendorSearchQuery, ignoreCase = true) ||
                                        it.categoryTag.contains(vendorSearchQuery, ignoreCase = true) ||
                                        it.phone.contains(vendorSearchQuery)
                            }
                        }

                        OutlinedTextField(
                            value = vendorSearchQuery,
                            onValueChange = {
                                vendorSearchQuery = it
                                dropdownExpanded = true
                            },
                            placeholder = { Text(if (isBangla) "দোকানের নাম লিখুন বা সিলেক্ট করুন..." else "Search or type store name...") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryEditable)
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            // First item: "+ নতুন শপ যুক্ত করুন"
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (vendorSearchQuery.isNotBlank() && activeVendors.none { it.name.equals(vendorSearchQuery.trim(), ignoreCase = true) })
                                                (if (isBangla) "নতুন শপ হিসেবে যোগ করুন: '${vendorSearchQuery.trim()}'" else "Add '${vendorSearchQuery.trim()}' as New Shop")
                                            else
                                                (if (isBangla) "+ নতুন শপ যুক্ত করুন" else "+ Add New Shop"),
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                onClick = {
                                    dropdownExpanded = false
                                    showCreateVendorDialog = true
                                }
                            )

                            filteredVendors.forEach { vendor ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Store,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = vendor.name,
                                                    fontWeight = FontWeight.SemiBold,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                if (vendor.categoryTag.isNotBlank() || vendor.phone.isNotBlank()) {
                                                    Text(
                                                        text = listOf(vendor.categoryTag, vendor.phone).filter { it.isNotBlank() }.joinToString(" • "),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedVendor = vendor
                                        vendorSearchQuery = vendor.name
                                        phone = vendor.phone
                                        locationNote = vendor.locationNote
                                        categoryTag = vendor.categoryTag
                                        dropdownExpanded = false
                                    }
                                )
                            }

                            if (filteredVendors.isEmpty() && vendorSearchQuery.isNotBlank()) {
                                Text(
                                    text = if (isBangla) "কোনো মিল পাওয়া যায়নি" else "No matching shop found",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(Localization.getString(Localization.Key.AMOUNT, isBangla)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(if (isBangla) "পণ্যের বিবরণ (ঐচ্ছিক)" else "Items / Note (Optional)") },
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
                        Text(Localization.getString(Localization.Key.CANCEL, isBangla))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            val finalName = selectedVendor?.name ?: vendorSearchQuery.trim()
                            if (finalName.isNotBlank() && amt > 0) {
                                val matched = selectedVendor ?: vendors.find { it.name.equals(finalName, true) }
                                onSave(
                                    matched?.id,
                                    finalName,
                                    amt,
                                    null,
                                    note.trim(),
                                    selectedVendor?.phone ?: phone.trim(),
                                    selectedVendor?.locationNote ?: locationNote.trim(),
                                    selectedVendor?.categoryTag ?: categoryTag.trim()
                                )
                            }
                        },
                        enabled = (selectedVendor != null || vendorSearchQuery.isNotBlank()) && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(Localization.getString(Localization.Key.SAVE, isBangla), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddNewVendorDialog(
    initialName: String = "",
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, locationNote: String, categoryTag: String) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf("") }
    var locationNote by remember { mutableStateOf("") }
    var categoryTag by remember { mutableStateOf("") }

    var phoneOptionsForSelection by remember { mutableStateOf<List<ContactPhoneOption>?>(null) }
    var pendingContactName by remember { mutableStateOf("") }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        if (uri != null) {
            val result = ContactUtils.extractContact(context, uri)
            if (result != null) {
                if (result.name.isNotBlank()) {
                    name = result.name
                }
                when {
                    result.phones.isEmpty() -> {
                        Toast.makeText(
                            context,
                            if (isBangla) "এই কন্টাক্টে কোনো ফোন নম্বর পাওয়া যায়নি" else "No phone numbers found in this contact",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    result.phones.size == 1 -> {
                        phone = result.phones.first().rawNumber
                    }
                    else -> {
                        pendingContactName = result.name
                        phoneOptionsForSelection = result.phones
                    }
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        contactPickerLauncher.launch(null)
    }

    val onPickContactClick = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        } else {
            contactPickerLauncher.launch(null)
        }
    }

    if (phoneOptionsForSelection != null) {
        SelectContactPhoneDialog(
            contactName = pendingContactName,
            options = phoneOptionsForSelection!!,
            isBangla = isBangla,
            onSelectPhone = { selected ->
                phone = selected.rawNumber
                phoneOptionsForSelection = null
            },
            onDismiss = {
                phoneOptionsForSelection = null
            }
        )
    }

    val presetCategories = remember(isBangla) {
        if (isBangla) listOf("মুদি দোকান", "ফার্মেসি", "কাঁচাবাজার", "রেস্তোরাঁ", "হার্ডওয়্যার", "জেনারেল স্টোর")
        else listOf("Grocery", "Pharmacy", "Fresh Market", "Restaurant", "Hardware", "General")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isBangla) "নতুন শপ যুক্ত করুন" else "Add New Shop",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isBangla) "দোকানের নাম *" else "Store / Shop Name *") },
                    leadingIcon = {
                        Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon = {
                        IconButton(onClick = onPickContactClick) {
                            Icon(Icons.Default.ContactPhone, contentDescription = if (isBangla) "কন্টাক্ট থেকে চুজ করুন" else "Pick from Contacts", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isBangla) "দোকানের ধরন / ক্যাটাগরি" else "Category / Type",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetCategories.forEach { cat ->
                        FilterChip(
                            selected = categoryTag.equals(cat, ignoreCase = true),
                            onClick = {
                                categoryTag = if (categoryTag.equals(cat, ignoreCase = true)) "" else cat
                            },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (isBangla) "মোবাইল নম্বর (ঐচ্ছিক)" else "Phone Number (Optional)") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon = {
                        IconButton(onClick = onPickContactClick) {
                            Icon(Icons.Default.Contacts, contentDescription = if (isBangla) "কন্টাক্ট থেকে চুজ করুন" else "Pick from Contacts", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = locationNote,
                    onValueChange = { locationNote = it },
                    label = { Text(if (isBangla) "ঠিকানা / এলাকা (ঐচ্ছিক)" else "Location / Address (Optional)") },
                    leadingIcon = {
                        Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
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
                        Text(Localization.getString(Localization.Key.CANCEL, isBangla))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(name.trim(), phone.trim(), locationNote.trim(), categoryTag.ifBlank { "General" }.trim())
                            }
                        },
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(Localization.getString(Localization.Key.SAVE, isBangla), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

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
    var amountText by remember { mutableStateOf(defaultAmount.toString()) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.account?.id ?: 0L) }
    var note by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(
                    text = "${if (isBangla) "বাকি পরিশোধ" else "Settle Shop Credit"} (${vendor.name})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(Localization.getString(Localization.Key.AMOUNT, isBangla)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(Localization.getString(Localization.Key.NOTE, isBangla)) },
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
                        Text(Localization.getString(Localization.Key.CANCEL, isBangla))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                onConfirm(selectedAccountId, amt, note.trim())
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isBangla) "পরিশোধ শোধ করুন" else "Confirm Settle", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
