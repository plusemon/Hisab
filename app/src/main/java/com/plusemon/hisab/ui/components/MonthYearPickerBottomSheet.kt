package com.plusemon.hisab.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plusemon.hisab.data.model.YearMonth
import com.plusemon.hisab.domain.util.Formatters
import com.plusemon.hisab.domain.util.Localization

/**
 * Modern, dark-mode compliant Month-Year Picker ModalBottomSheet.
 * Allows interactive switching across years and selecting any month (January to December),
 * with quick-action chips for "This Month" and "Last Month", and prominent highlight on active selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthYearPickerBottomSheet(
    selectedMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit,
    onDismiss: () -> Unit,
    isBangla: Boolean,
    useBanglaDigits: Boolean,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val now = remember { YearMonth.now() }
    var pickerYear by remember(selectedMonth.year) { mutableIntStateOf(selectedMonth.year) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color(0xFF161F30),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = Color(0xFF475569)
            )
        },
        modifier = modifier.testTag("month_year_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header: Title and Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color(0xFF2DD4BF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = Localization.getString(Localization.Key.SELECT_MONTH, isBangla),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF1F5F9)
                        )
                        Text(
                            text = Formatters.formatMonthYear(selectedMonth.toMonthYearString(), isBangla),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("month_picker_close_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = Localization.getString(Localization.Key.CANCEL, isBangla),
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Action Chips: "This Month", "Last Month", and jump to current year
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "This Month" Chip
                val isThisMonthSelected = selectedMonth.isCurrent()
                FilterChip(
                    selected = isThisMonthSelected,
                    onClick = {
                        onMonthSelected(now)
                        onDismiss()
                    },
                    label = {
                        Text(
                            text = Localization.getString(Localization.Key.THIS_MONTH, isBangla),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isThisMonthSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFFCBD5E1),
                        iconColor = Color(0xFF94A3B8),
                        selectedContainerColor = Color(0xFF0F766E),
                        selectedLabelColor = Color(0xFFFFFFFF),
                        selectedLeadingIconColor = Color(0xFF2DD4BF)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isThisMonthSelected) Color(0xFF2DD4BF) else Color(0xFF334155).copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.testTag("quick_chip_this_month")
                )

                // "Last Month" Chip
                val lastMonth = remember { now.minusMonths(1) }
                val isLastMonthSelected = selectedMonth == lastMonth
                FilterChip(
                    selected = isLastMonthSelected,
                    onClick = {
                        onMonthSelected(lastMonth)
                        onDismiss()
                    },
                    label = {
                        Text(
                            text = Localization.getString(Localization.Key.LAST_MONTH, isBangla),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isLastMonthSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFFCBD5E1),
                        iconColor = Color(0xFF94A3B8),
                        selectedContainerColor = Color(0xFF0F766E),
                        selectedLabelColor = Color(0xFFFFFFFF),
                        selectedLeadingIconColor = Color(0xFF2DD4BF)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isLastMonthSelected) Color(0xFF2DD4BF) else Color(0xFF334155).copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.testTag("quick_chip_last_month")
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Year Selector Bar
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { pickerYear -= 1 },
                        modifier = Modifier.testTag("month_picker_prev_year_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = Localization.getString(Localization.Key.CHANGE_YEAR, isBangla),
                            tint = Color(0xFFE2E8F0)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val yearText = if (useBanglaDigits) Formatters.toBanglaDigits(pickerYear.toString()) else pickerYear.toString()
                        Text(
                            text = yearText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFFFFF),
                            modifier = Modifier.testTag("month_picker_year_text")
                        )

                        if (pickerYear != now.year) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF0F766E).copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, Color(0xFF2DD4BF).copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { pickerYear = now.year }
                            ) {
                                Text(
                                    text = if (useBanglaDigits) Formatters.toBanglaDigits(now.year.toString()) else now.year.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF2DD4BF),
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { pickerYear += 1 },
                        modifier = Modifier.testTag("month_picker_next_year_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = Localization.getString(Localization.Key.CHANGE_YEAR, isBangla),
                            tint = Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 12 Months Grid: 4 rows x 3 columns
            val months = (1..12).toList()
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (rowIndex in 0 until 4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (colIndex in 0 until 3) {
                            val monthNumber = months[rowIndex * 3 + colIndex]
                            val isSelected = pickerYear == selectedMonth.year && monthNumber == selectedMonth.month
                            val isRealCurrentMonth = pickerYear == now.year && monthNumber == now.month

                            MonthGridCell(
                                monthNumber = monthNumber,
                                isSelected = isSelected,
                                isCurrentMonth = isRealCurrentMonth,
                                isBangla = isBangla,
                                onClick = {
                                    val newSelection = YearMonth(pickerYear, monthNumber)
                                    onMonthSelected(newSelection)
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer Cancel / Dismiss
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("month_picker_cancel_btn")
                ) {
                    Text(
                        text = Localization.getString(Localization.Key.CANCEL, isBangla),
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthGridCell(
    monthNumber: Int,
    isSelected: Boolean,
    isCurrentMonth: Boolean,
    isBangla: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthName = Formatters.getMonthName(monthNumber, isBangla)
    val shortName = Formatters.getMonthShortName(monthNumber, isBangla)

    val containerColor = when {
        isSelected -> Color(0xFF0F766E)
        isCurrentMonth -> Color(0xFF1E293B)
        else -> Color(0xFF162235)
    }

    val borderColor = when {
        isSelected -> Color(0xFF2DD4BF)
        isCurrentMonth -> Color(0xFF2DD4BF).copy(alpha = 0.5f)
        else -> Color(0xFF334155).copy(alpha = 0.6f)
    }

    val textColor = when {
        isSelected -> Color(0xFFFFFFFF)
        isCurrentMonth -> Color(0xFF2DD4BF)
        else -> Color(0xFFE2E8F0)
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("month_cell_$monthNumber")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        fontWeight = if (isSelected || isCurrentMonth) FontWeight.Bold else FontWeight.Medium,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF2DD4BF),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                if (isCurrentMonth && !isSelected) {
                    Text(
                        text = if (isBangla) "চলতি" else "Current",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color(0xFF2DD4BF),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
