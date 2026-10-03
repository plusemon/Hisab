package com.plusemon.hisab.ui.screens.nlp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.plusemon.hisab.domain.util.Formatters
import com.plusemon.hisab.domain.util.Localization
import com.plusemon.hisab.domain.util.ParsedQuickEntry
import com.plusemon.hisab.ui.components.CategoryIconBadge
import com.plusemon.hisab.ui.components.CurrencyAmountText
import com.plusemon.hisab.ui.theme.ExpenseRed
import com.plusemon.hisab.ui.theme.IncomeGreen
import com.plusemon.hisab.ui.theme.TransferBlue
import com.plusemon.hisab.ui.viewmodel.HisabViewModel

@Composable
fun QuickEntryDialog(
    viewModel: HisabViewModel,
    isBangla: Boolean,
    onDismiss: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var parsedEntry by remember { mutableStateOf<ParsedQuickEntry?>(null) }

    fun updateInput(text: String) {
        inputText = text
        parsedEntry = viewModel.parseQuickEntry(text)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Localization.getString(Localization.Key.NATURAL_ENTRY_TITLE, isBangla),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = Localization.getString(Localization.Key.NATURAL_ENTRY_HINT, isBangla),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Input text field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { updateInput(it) },
                    placeholder = {
                        Text(if (isBangla) "যেমন: চা নাস্তা ৫০ নগদ" else "e.g. lunch 250 cash")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nlp_quick_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick suggestions chips
                val suggestions = if (isBangla) listOf(
                    "বাজার ১২০০ বিকাশ",
                    "চা নাস্তা ৪০ নগদ",
                    "রিকশা ৫০ নগদ",
                    "বেতন ৫০০০০ ব্যাংক"
                ) else listOf(
                    "lunch 250 cash",
                    "groceries 1200 bkash",
                    "uber 350 card",
                    "salary 50000 bank"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    suggestions.take(2).forEach { sample ->
                        SuggestionChip(
                            onClick = { updateInput(sample) },
                            label = { Text(sample, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live Parsed Preview Box
                AnimatedVisibility(visible = parsedEntry != null) {
                    val entry = parsedEntry
                    if (entry != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = if (isBangla) "সনাক্তকৃত হিসাব:" else "Detected Entry:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Amount & Type
                                    val (typeColor, typeLabel) = when (entry.type) {
                                        com.plusemon.hisab.data.model.TransactionType.EXPENSE -> Pair(ExpenseRed, if (isBangla) "খরচ" else "Expense")
                                        com.plusemon.hisab.data.model.TransactionType.INCOME -> Pair(IncomeGreen, if (isBangla) "আয়" else "Income")
                                        com.plusemon.hisab.data.model.TransactionType.TRANSFER -> Pair(TransferBlue, if (isBangla) "স্থানান্তর" else "Transfer")
                                    }

                                    Column {
                                        CurrencyAmountText(
                                            amount = entry.amount,
                                            currencySymbol = "৳",
                                            useBanglaDigits = isBangla,
                                            color = typeColor,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = typeLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = typeColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Category or Account badge
                                    if (entry.matchedCategory != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CategoryIconBadge(
                                                iconName = entry.matchedCategory.iconName,
                                                colorHex = entry.matchedCategory.colorHex,
                                                size = 36.dp,
                                                iconSize = 18.dp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = entry.matchedCategory.localizedName(isBangla),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                if (entry.matchedAccount != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${if (isBangla) "অ্যাকাউন্ট" else "Account"}: ${entry.matchedAccount.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
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
                            parsedEntry?.let {
                                viewModel.addParsedTransaction(it)
                                onDismiss()
                            }
                        },
                        enabled = parsedEntry != null,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("nlp_submit_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Localization.getString(Localization.Key.SAVE, isBangla))
                    }
                }
            }
        }
    }
}
