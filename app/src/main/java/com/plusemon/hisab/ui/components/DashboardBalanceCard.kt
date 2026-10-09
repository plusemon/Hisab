package com.plusemon.hisab.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plusemon.hisab.domain.util.Formatters
import com.plusemon.hisab.domain.util.Localization
import com.plusemon.hisab.ui.theme.EmeraldPrimary

/**
 * Modern, adaptive Dashboard Balance & Monthly Overview Hero Card.
 * Dynamically supports both crisp Light Mode and sleek Dark Mode with
 * appropriate contrast, borders, and fintech indicators.
 */
@Composable
fun DashboardBalanceCard(
    totalBalance: Double,
    monthIncome: Double,
    monthExpense: Double,
    currencySymbol: String,
    useBanglaDigits: Boolean,
    hideBalances: Boolean,
    isBangla: Boolean,
    currentMonthYear: String = Formatters.getCurrentMonthYear(),
    onMonthClick: (() -> Unit)? = null,
    onIncomeClick: (() -> Unit)? = null,
    onExpenseClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isCurrentMonth = currentMonthYear == Formatters.getCurrentMonthYear()
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    // Theme adaptive colors
    val cardBackgroundBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF161F30),
                Color(0xFF0F1A2A)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF8FAFC)
            )
        )
    }

    val cardBorderColor = if (isDark) Color(0xFF1E3A4A) else MaterialTheme.colorScheme.outlineVariant
    val titleColor = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
    val monthChipBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    val monthChipBorder = if (isDark) Color(0xFF334155).copy(alpha = 0.8f) else MaterialTheme.colorScheme.outlineVariant
    val monthChipIconTint = if (isDark) Color(0xFF2DD4BF) else EmeraldPrimary
    val monthChipTextColor = if (isDark) Color(0xFFE2E8F0) else MaterialTheme.colorScheme.onSurface
    val monthChipChevronTint = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
    val heroAmountColor = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("dashboard_balance_hero_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF161F30) else MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = cardBackgroundBrush)
                .drawBehind {
                    // Subtle top radial glow for sleek, modern aesthetic
                    val glowColors = if (isDark) {
                        listOf(
                            Color(0xFF2DD4BF).copy(alpha = 0.07f),
                            Color(0xFF0F766E).copy(alpha = 0.03f),
                            Color.Transparent
                        )
                    } else {
                        listOf(
                            Color(0xFF0F766E).copy(alpha = 0.035f),
                            Color(0xFF2DD4BF).copy(alpha = 0.015f),
                            Color.Transparent
                        )
                    }
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = glowColors,
                            center = Offset(size.width * 0.5f, 0f),
                            radius = size.width * 0.75f
                        ),
                        radius = size.width * 0.75f,
                        center = Offset(size.width * 0.5f, 0f)
                    )
                }
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Row: Title + Interactive Month Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCurrentMonth) {
                            Localization.getString(Localization.Key.TOTAL_BALANCE, isBangla)
                        } else {
                            Localization.getString(Localization.Key.MONTH_NET_BALANCE, isBangla)
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = titleColor,
                        fontWeight = FontWeight.Medium
                    )

                    // Interactive Month Chip (clickable with tactile feedback & downward chevron)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = monthChipBg,
                        border = BorderStroke(1.dp, monthChipBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .then(
                                if (onMonthClick != null) {
                                    Modifier.clickable(onClick = onMonthClick)
                                } else Modifier
                            )
                            .testTag("hero_month_picker_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = monthChipIconTint,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Formatters.formatMonthYear(currentMonthYear, isBangla),
                                style = MaterialTheme.typography.labelSmall,
                                color = monthChipTextColor,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isBangla) "মাস নির্বাচন করুন" else "Select Month",
                                tint = monthChipChevronTint,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hero Big Amount Number & Net Difference Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CurrencyAmountText(
                        amount = totalBalance,
                        currencySymbol = currencySymbol,
                        useBanglaDigits = useBanglaDigits,
                        hideBalances = hideBalances,
                        color = heroAmountColor,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 32.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .testTag("hero_total_balance_text")
                    )

                    // Difference Badge: Crisp green on light-green/dark-green, rose on light-red/dark-red
                    val netSavings = monthIncome - monthExpense
                    val hasTransactions = monthIncome > 0 || monthExpense > 0
                    val isPositive = netSavings >= 0

                    val diffBg = when {
                        !hasTransactions -> if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                        isPositive -> if (isDark) Color(0xFF14382A) else Color(0xFFDCFCE7)
                        else -> if (isDark) Color(0xFF4C1D24) else Color(0xFFFEE2E2)
                    }

                    val diffBorder = when {
                        !hasTransactions -> if (isDark) Color(0xFF334155).copy(alpha = 0.60f) else Color(0xFFE2E8F0)
                        isPositive -> if (isDark) Color(0xFF059669).copy(alpha = 0.40f) else Color(0xFF86EFAC).copy(alpha = 0.8f)
                        else -> if (isDark) Color(0xFFDC2626).copy(alpha = 0.40f) else Color(0xFFFCA5A5).copy(alpha = 0.8f)
                    }

                    val diffTextColor = when {
                        !hasTransactions -> if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        isPositive -> if (isDark) Color(0xFF4ADE80) else Color(0xFF15803D)
                        else -> if (isDark) Color(0xFFF87171) else Color(0xFFB91C1C)
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = diffBg,
                        border = BorderStroke(1.dp, diffBorder),
                        modifier = Modifier
                            .padding(bottom = 4.dp, start = 8.dp)
                            .testTag("hero_difference_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (hasTransactions) {
                                Text(
                                    text = if (isPositive) "+ " else "- ",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = diffTextColor
                                )
                            }
                            CurrencyAmountText(
                                amount = if (hasTransactions) kotlin.math.abs(netSavings) else 0.0,
                                currencySymbol = currencySymbol,
                                useBanglaDigits = useBanglaDigits,
                                hideBalances = hideBalances,
                                color = diffTextColor,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Income & Expense Sub-Cards
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val isVeryNarrow = maxWidth < 310.dp

                    if (isVeryNarrow) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IncomeSubCard(
                                amount = monthIncome,
                                currencySymbol = currencySymbol,
                                useBanglaDigits = useBanglaDigits,
                                hideBalances = hideBalances,
                                isBangla = isBangla,
                                isDark = isDark,
                                isCurrentMonth = isCurrentMonth,
                                onClick = onIncomeClick,
                                modifier = Modifier.fillMaxWidth()
                            )
                            ExpenseSubCard(
                                amount = monthExpense,
                                currencySymbol = currencySymbol,
                                useBanglaDigits = useBanglaDigits,
                                hideBalances = hideBalances,
                                isBangla = isBangla,
                                isDark = isDark,
                                isCurrentMonth = isCurrentMonth,
                                onClick = onExpenseClick,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IncomeSubCard(
                                amount = monthIncome,
                                currencySymbol = currencySymbol,
                                useBanglaDigits = useBanglaDigits,
                                hideBalances = hideBalances,
                                isBangla = isBangla,
                                isDark = isDark,
                                isCurrentMonth = isCurrentMonth,
                                onClick = onIncomeClick,
                                modifier = Modifier.weight(1f)
                            )
                            ExpenseSubCard(
                                amount = monthExpense,
                                currencySymbol = currencySymbol,
                                useBanglaDigits = useBanglaDigits,
                                hideBalances = hideBalances,
                                isBangla = isBangla,
                                isDark = isDark,
                                isCurrentMonth = isCurrentMonth,
                                onClick = onExpenseClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IncomeSubCard(
    amount: Double,
    currencySymbol: String,
    useBanglaDigits: Boolean,
    hideBalances: Boolean,
    isBangla: Boolean,
    isDark: Boolean,
    isCurrentMonth: Boolean = true,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val subCardBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val subCardBorder = if (isDark) Color(0xFF334155).copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant
    val iconContainerBg = if (isDark) Color(0xFF064E3B) else Color(0xFFDCFCE7)
    val iconTint = if (isDark) Color(0xFF34D399) else Color(0xFF16A34A)
    val labelColor = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
    val amountColor = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .testTag("hero_income_subcard"),
        shape = RoundedCornerShape(14.dp),
        color = subCardBg,
        border = BorderStroke(1.dp, subCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(iconContainerBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                val incomeLabel = if (isCurrentMonth) {
                    Localization.getString(Localization.Key.INCOME_THIS_MONTH, isBangla)
                } else {
                    if (isBangla) "আয় (মাস)" else "Income (Month)"
                }
                Text(
                    text = incomeLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = labelColor,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            CurrencyAmountText(
                amount = amount,
                currencySymbol = currencySymbol,
                useBanglaDigits = useBanglaDigits,
                hideBalances = hideBalances,
                color = amountColor,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ExpenseSubCard(
    amount: Double,
    currencySymbol: String,
    useBanglaDigits: Boolean,
    hideBalances: Boolean,
    isBangla: Boolean,
    isDark: Boolean,
    isCurrentMonth: Boolean = true,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val subCardBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val subCardBorder = if (isDark) Color(0xFF334155).copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant
    val iconContainerBg = if (isDark) Color(0xFF4C1D24) else Color(0xFFFEE2E2)
    val iconTint = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
    val labelColor = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
    val amountColor = if (isDark) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .testTag("hero_expense_subcard"),
        shape = RoundedCornerShape(14.dp),
        color = subCardBg,
        border = BorderStroke(1.dp, subCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(iconContainerBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                val expenseLabel = if (isCurrentMonth) {
                    Localization.getString(Localization.Key.EXPENSES_THIS_MONTH, isBangla)
                } else {
                    if (isBangla) "খরচ (মাস)" else "Expenses (Month)"
                }
                Text(
                    text = expenseLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = labelColor,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            CurrencyAmountText(
                amount = amount,
                currencySymbol = currencySymbol,
                useBanglaDigits = useBanglaDigits,
                hideBalances = hideBalances,
                color = amountColor,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
