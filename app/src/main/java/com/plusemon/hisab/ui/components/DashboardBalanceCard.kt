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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plusemon.hisab.domain.util.Formatters
import com.plusemon.hisab.domain.util.Localization

/**
 * Modernized, dark-mode integrated Dashboard Balance & Monthly Overview Hero Card.
 * Adheres to dark surface hierarchy with high-contrast typography, dedicated dark surface tiles,
 * and sleek fintech indicators.
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
    onTogglePrivacy: (() -> Unit)? = null,
    onMonthClick: (() -> Unit)? = null,
    onIncomeClick: (() -> Unit)? = null,
    onExpenseClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isCurrentMonth = currentMonthYear == Formatters.getCurrentMonthYear()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("dashboard_balance_hero_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
        border = BorderStroke(1.dp, Color(0xFF1E3A4A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF161F30),
                            Color(0xFF0F1A2A)
                        )
                    )
                )
                .drawBehind {
                    // Subtle top radial glow for a sleek, modern fintech aesthetic
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF2DD4BF).copy(alpha = 0.07f),
                                Color(0xFF0F766E).copy(alpha = 0.03f),
                                Color.Transparent
                            ),
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
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )

                    // Interactive Month Chip (clickable with tactile feedback & downward chevron)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.8f)),
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
                                tint = Color(0xFF2DD4BF),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Formatters.formatMonthYear(currentMonthYear, isBangla),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE2E8F0),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isBangla) "মাস নির্বাচন করুন" else "Select Month",
                                tint = Color(0xFF94A3B8),
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
                        color = Color(0xFFFFFFFF),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 32.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .testTag("hero_total_balance_text")
                    )

                    // Difference Badge: Crisp green on dark-green, rose on dark-red, or slate for zero
                    val netSavings = monthIncome - monthExpense
                    val hasTransactions = monthIncome > 0 || monthExpense > 0
                    val isPositive = netSavings >= 0

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            !hasTransactions -> Color(0xFF1E293B)
                            isPositive -> Color(0xFF14382A)
                            else -> Color(0xFF4C1D24)
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                !hasTransactions -> Color(0xFF334155).copy(alpha = 0.60f)
                                isPositive -> Color(0xFF059669).copy(alpha = 0.40f)
                                else -> Color(0xFFDC2626).copy(alpha = 0.40f)
                            }
                        ),
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
                                    color = if (isPositive) Color(0xFF4ADE80) else Color(0xFFF87171)
                                )
                            }
                            CurrencyAmountText(
                                amount = if (hasTransactions) kotlin.math.abs(netSavings) else 0.0,
                                currencySymbol = currencySymbol,
                                useBanglaDigits = useBanglaDigits,
                                hideBalances = hideBalances,
                                color = when {
                                    !hasTransactions -> Color(0xFF94A3B8)
                                    isPositive -> Color(0xFF4ADE80)
                                    else -> Color(0xFFF87171)
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Dedicated Dark Surface Tiles for Income & Expense Sub-Cards
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val isVeryNarrow = maxWidth < 310.dp

                    if (isVeryNarrow) {
                        // Vertical stacking for extreme narrow screens
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
                                isCurrentMonth = isCurrentMonth,
                                onClick = onExpenseClick,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        // Side-by-side with clear visual indicators
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
    isCurrentMonth: Boolean = true,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .testTag("hero_income_subcard"),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1E293B),
        border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp)
        ) {
            // Header: Subtle Green Icon Container + Muted Slate Label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF064E3B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
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
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Amount: High contrast crisp white value
            CurrencyAmountText(
                amount = amount,
                currencySymbol = currencySymbol,
                useBanglaDigits = useBanglaDigits,
                hideBalances = hideBalances,
                color = Color(0xFFFFFFFF),
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
    isCurrentMonth: Boolean = true,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .testTag("hero_expense_subcard"),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1E293B),
        border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp)
        ) {
            // Header: Subtle Red/Rose Icon Container + Muted Slate Label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4C1D24)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = Color(0xFFF87171),
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
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Amount: High contrast crisp white value
            CurrencyAmountText(
                amount = amount,
                currencySymbol = currencySymbol,
                useBanglaDigits = useBanglaDigits,
                hideBalances = hideBalances,
                color = Color(0xFFFFFFFF),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
