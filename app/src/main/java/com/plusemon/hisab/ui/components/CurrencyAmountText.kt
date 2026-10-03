package com.plusemon.hisab.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.plusemon.hisab.domain.util.Formatters

@Composable
fun CurrencyAmountText(
    amount: Double,
    currencySymbol: String = "৳",
    useBanglaDigits: Boolean = false,
    hideBalances: Boolean = false,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    fontWeight: FontWeight? = null,
    prefix: String = ""
) {
    val formatted = Formatters.formatAmount(
        amount = amount,
        currencySymbol = currencySymbol,
        useBanglaDigits = useBanglaDigits,
        hideBalances = hideBalances
    )

    val displayText = if (hideBalances) formatted else "$prefix$formatted"

    Text(
        text = displayText,
        color = color,
        style = style,
        fontWeight = fontWeight,
        modifier = modifier
    )
}
