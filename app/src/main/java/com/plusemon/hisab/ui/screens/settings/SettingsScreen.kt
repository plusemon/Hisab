package com.plusemon.hisab.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.plusemon.hisab.domain.util.Localization
import com.plusemon.hisab.ui.theme.ExpenseRed
import com.plusemon.hisab.ui.viewmodel.HisabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: HisabViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRecurring: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val isBn = settings.language == "bn"
    val currentUser by viewModel.currentUser.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showUpdateCheckDialog by remember { mutableStateOf(false) }

    // PIN Setup Dialog
    if (showPinDialog) {
        SetPinDialog(
            isBangla = isBn,
            onDismiss = { showPinDialog = false },
            onSetPin = { pin ->
                viewModel.setPin(pin)
                showPinDialog = false
                Toast.makeText(context, if (isBn) "পিন সুরক্ষা চালু হয়েছে" else "PIN lock enabled", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Import CSV Dialog
    if (showImportDialog) {
        ImportCsvDialog(
            isBangla = isBn,
            onDismiss = { showImportDialog = false },
            onImport = { csvContent ->
                viewModel.importTransactionsFromCsv(csvContent)
                showImportDialog = false
            }
        )
    }

    // Clear Data Confirmation Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text(if (isBn) "সব তথ্য মুছুন?" else "Clear All Data?") },
            text = { Text(if (isBn) "আপনার সব লেনদেন, বাজেট এবং হিসাব মুছে যাবে।" else "All your transactions, budgets and records will be deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text(Localization.getString(Localization.Key.DELETE, isBn))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text(Localization.getString(Localization.Key.CANCEL, isBn))
                }
            }
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text(Localization.getString(Localization.Key.DELETE_ACCOUNT, isBn)) },
            text = { Text(Localization.getString(Localization.Key.DELETE_ACCOUNT_CONFIRM, isBn)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccount()
                        showDeleteAccountDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text(Localization.getString(Localization.Key.DELETE, isBn))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text(Localization.getString(Localization.Key.CANCEL, isBn))
                }
            }
        )
    }

    // Check for Updates Dialog
    if (showUpdateCheckDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateCheckDialog = false },
            icon = { Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(if (isBn) "অ্যাপ আপডেট" else "App Updates") },
            text = { Text(if (isBn) "আপনি হিসাব-এর সর্বশেষ ভার্সন (v1.0) ব্যবহার করছেন।" else "You are using the latest version of Hisab (v1.0).") },
            confirmButton = {
                Button(onClick = { showUpdateCheckDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Localization.getString(Localization.Key.SETTINGS, isBn),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
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
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser?.displayName?.take(2)?.uppercase() ?: "U",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.displayName ?: "User",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentUser?.email ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section: Localization & Language
            Text(
                text = if (isBn) "ভাষা ও প্রদর্শন" else "Language & Display",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.Language,
                        title = Localization.getString(Localization.Key.LANGUAGE, isBn),
                        value = if (isBn) "বাংলা" else "English",
                        onClick = { viewModel.toggleLanguage() }
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRowItem(
                        icon = Icons.Default.Language,
                        title = Localization.getString(Localization.Key.NUMERAL_SYSTEM, isBn),
                        value = if (settings.numeralSystem == "bn") "বাংলা (১, ২, ৩)" else "English (1, 2, 3)",
                        onClick = { viewModel.toggleNumeralSystem() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section: Security & Tools
            Text(
                text = Localization.getString(Localization.Key.SECURITY, isBn),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    // PIN Lock Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = Localization.getString(Localization.Key.PIN_LOCK, isBn),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Switch(
                            checked = settings.pinEnabled,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    showPinDialog = true
                                } else {
                                    viewModel.disablePin()
                                }
                            }
                        )
                    }

                    Divider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Recurring Transactions
                    SettingsRowItem(
                        icon = Icons.Default.Repeat,
                        title = Localization.getString(Localization.Key.RECURRING_TRANSACTIONS, isBn),
                        onClick = onNavigateToRecurring
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section: Data & Backup
            Text(
                text = Localization.getString(Localization.Key.DATA_PORTABILITY, isBn),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.ContentCopy,
                        title = Localization.getString(Localization.Key.EXPORT_SUMMARY, isBn),
                        onClick = {
                            val summary = viewModel.generateFinancialSummary()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Hisab Summary", summary))
                            Toast.makeText(context, if (isBn) "সারাংশ কপি করা হয়েছে" else "Summary copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRowItem(
                        icon = Icons.Default.FileDownload,
                        title = Localization.getString(Localization.Key.EXPORT_CSV, isBn),
                        onClick = {
                            val csvData = viewModel.exportTransactionsCsv()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Hisab CSV", csvData))
                            Toast.makeText(context, if (isBn) "CSV ডাটা কপি করা হয়েছে" else "CSV data copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRowItem(
                        icon = Icons.Default.CloudUpload,
                        title = Localization.getString(Localization.Key.IMPORT_CSV, isBn),
                        onClick = { showImportDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section: About & Updates
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.SystemUpdate,
                        title = Localization.getString(Localization.Key.CHECK_UPDATES, isBn),
                        value = "v1.0 (Latest)",
                        onClick = { showUpdateCheckDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section: Danger & Logout
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.Delete,
                        title = if (isBn) "সব তথ্য পরিষ্কার করুন" else "Clear All Data",
                        titleColor = ExpenseRed,
                        onClick = { showClearDataDialog = true }
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRowItem(
                        icon = Icons.Default.DeleteForever,
                        title = Localization.getString(Localization.Key.DELETE_ACCOUNT, isBn),
                        titleColor = ExpenseRed,
                        onClick = { showDeleteAccountDialog = true }
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRowItem(
                        icon = Icons.Default.ExitToApp,
                        title = Localization.getString(Localization.Key.SIGN_OUT, isBn),
                        titleColor = ExpenseRed,
                        onClick = { viewModel.signOut() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = titleColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun SetPinDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSetPin: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(
                    text = Localization.getString(Localization.Key.SET_PIN, isBangla),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) pin = it },
                    label = { Text(if (isBangla) "৪ ডিজিটের পিন" else "4-Digit PIN") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) confirmPin = it },
                    label = { Text(Localization.getString(Localization.Key.CONFIRM_PIN, isBangla)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (isError) {
                    Text(
                        text = if (isBangla) "পিন দুটি মিলছে না বা ৪ ডিজিট নয়" else "PINs do not match or are not 4 digits",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

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
                            if (pin.length == 4 && pin == confirmPin) {
                                onSetPin(pin)
                            } else {
                                isError = true
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(Localization.getString(Localization.Key.SAVE, isBangla))
                    }
                }
            }
        }
    }
}

@Composable
fun ImportCsvDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var rawCsvText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(
                    text = Localization.getString(Localization.Key.IMPORT_CSV, isBangla),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isBangla) "CSV ডাটা নিচে পেস্ট করুন:" else "Paste CSV data below:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = rawCsvText,
                    onValueChange = { rawCsvText = it },
                    placeholder = { Text("ID,Date,Type,Amount,Currency,Account,ToAccount,Category,Fee,Note...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(12.dp)
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
                            if (rawCsvText.isNotBlank()) {
                                onImport(rawCsvText.trim())
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isBangla) "ইমপোর্ট করুন" else "Import")
                    }
                }
            }
        }
    }
}
