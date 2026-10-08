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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
 * Modern, responsive Dashboard Balance & Monthly Overview Card.
 * Fully adapts to any screen width and font scale without text wrapping issues.
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
    onIncomeClick: (() -> Unit)? = null,
    onExpenseClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0D5E56),
                            Color(0xFF0F766E),
                            Color(0xFF042F2E)
                        )
                    )
                )
                .drawBehind {
                    // Soft ambient decorative light glow in top-right
                    drawCircle(
                        color = Color(0xFF2DD4BF).copy(alpha = 0.16f),
                        radius = size.width * 0.40f,
                        center = Offset(size.width * 0.88f, size.height * 0.15f)
                    )
                    // Secondary subtle glow in bottom-left
                    drawCircle(
                        color = Color(0xFF14B8A6).copy(alpha = 0.10f),
                        radius = size.width * 0.35f,
                        center = Offset(size.width * 0.08f, size.height * 0.90f)
                    )
                }
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Row: Title + Month Chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = Localization.getString(Localization.Key.TOTAL_BALANCE, isBangla),
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White.copy(alpha = 0.90f),
                            fontWeight = FontWeight.Medium
                        )
                        if (onTogglePrivacy != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = onTogglePrivacy,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (hideBalances) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Balance",
                                    tint = Color.White.copy(alpha = 0.75f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Month Chip (frosted glass aesthetic)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = Formatters.formatMonthYear(currentMonthYear, isBangla),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hero Big Amount Number & Net Cashflow Indicator
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
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 32.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .testTag("hero_total_balance_text")
                    )

                    // Optional Net Cash Flow Badge (Income - Expense)
                    val netSavings = monthIncome - monthExpense
                    if (monthIncome > 0 || monthExpense > 0) {
                        val isPositive = netSavings >= 0
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isPositive) Color(0xFF10B981).copy(alpha = 0.22f) else Color(0xFFEF4444).copy(alpha = 0.22f),
                            border = BorderStroke(
                                1.dp,
                                if (isPositive) Color(0xFF34D399).copy(alpha = 0.35f) else Color(0xFFF87171).copy(alpha = 0.35f)
                            ),
                            modifier = Modifier.padding(bottom = 4.dp, start = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isPositive) "+" else "-",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPositive) Color(0xFFA7F3D0) else Color(0xFFFECACA)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                CurrencyAmountText(
                                    amount = kotlin.math.abs(netSavings),
                                    currencySymbol = currencySymbol,
                                    useBanglaDigits = useBanglaDigits,
                                    hideBalances = hideBalances,
                                    color = if (isPositive) Color(0xFFA7F3D0) else Color(0xFFFECACA),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Responsive Sub-Cards for Income and Expenses
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
                                onClick = onIncomeClick,
                                modifier = Modifier.fillMaxWidth()
                            )
                            ExpenseSubCard(
                                amount = monthExpense,
                                currencySymbol = currencySymbol,
                                useBanglaDigits = useBanglaDigits,
                                hideBalances = hideBalances,
                                isBangla = isBangla,
                                onClick = onExpenseClick,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        // Side-by-side with vertical hierarchy inside each card
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
                                onClick = onIncomeClick,
                                modifier = Modifier.weight(1f)
                            )
                            ExpenseSubCard(
                                amount = monthExpense,
                                currencySymbol = currencySymbol,
                                useBanglaDigits = useBanglaDigits,
                                hideBalances = hideBalances,
                                isBangla = isBangla,
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
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // Header: Icon + Label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = 0.30f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = Color(0xFF6EE7B7),
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Localization.getString(Localization.Key.INCOME_THIS_MONTH, isBangla),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Amount: Full width of card prevents unwanted text wrapping!
            CurrencyAmountText(
                amount = amount,
                currencySymbol = currencySymbol,
                useBanglaDigits = useBanglaDigits,
                hideBalances = hideBalances,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
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
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // Header: Icon + Label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444).copy(alpha = 0.30f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = Color(0xFFFCA5A5),
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Localization.getString(Localization.Key.EXPENSES_THIS_MONTH, isBangla),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Amount: Full width of card prevents unwanted text wrapping!
            CurrencyAmountText(
                amount = amount,
                currencySymbol = currencySymbol,
                useBanglaDigits = useBanglaDigits,
                hideBalances = hideBalances,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
