package com.plusemon.hisab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun getIconByName(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "restaurant", "food" -> Icons.Default.Restaurant
        "shopping_cart", "grocery" -> Icons.Default.ShoppingCart
        "directions_bus", "transport" -> Icons.Default.DirectionsBus
        "receipt_long", "bills" -> Icons.Default.ReceiptLong
        "shopping_bag", "shopping" -> Icons.Default.ShoppingBag
        "medical_services", "health" -> Icons.Default.MedicalServices
        "movie", "entertainment" -> Icons.Default.Movie
        "school", "education" -> Icons.Default.School
        "payments", "salary", "cash" -> Icons.Default.Payments
        "store", "business" -> Icons.Default.Store
        "laptop", "freelance" -> Icons.Default.Laptop
        "trending_up", "investment" -> Icons.Default.TrendingUp
        "card_giftcard", "gift" -> Icons.Default.CardGiftcard
        "account_balance", "bank" -> Icons.Default.AccountBalance
        "account_balance_wallet", "wallet", "bkash", "nagad" -> Icons.Default.AccountBalanceWallet
        "credit_card", "card" -> Icons.Default.CreditCard
        "savings" -> Icons.Default.Savings
        "pie_chart" -> Icons.Default.PieChart
        else -> Icons.Default.Category
    }
}

fun parseColorHex(hex: String, defaultColor: Color = Color(0xFF0F766E)): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = if (clean.length == 6) {
            android.graphics.Color.parseColor("#$clean")
        } else if (clean.length == 8) {
            android.graphics.Color.parseColor("#$clean")
        } else {
            return defaultColor
        }
        Color(colorInt)
    } catch (e: Exception) {
        defaultColor
    }
}

@Composable
fun CategoryIconBadge(
    iconName: String,
    colorHex: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    isRounded: Boolean = true
) {
    val mainColor = parseColorHex(colorHex)
    val bgColor = mainColor.copy(alpha = 0.15f)
    val shape = if (isRounded) RoundedCornerShape(12.dp) else CircleShape

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getIconByName(iconName),
            contentDescription = null,
            tint = mainColor,
            modifier = Modifier.size(iconSize)
        )
    }
}
