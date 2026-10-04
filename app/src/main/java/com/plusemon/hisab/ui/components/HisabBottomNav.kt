package com.plusemon.hisab.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plusemon.hisab.domain.util.Localization

data class NavItem(
    val route: String,
    val titleKey: Localization.Key,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun HisabBottomNav(
    currentRoute: String,
    isBangla: Boolean,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem("dashboard", Localization.Key.APP_NAME, Icons.Default.Home, "nav_dashboard"),
        NavItem("transactions", Localization.Key.RECENT_TRANSACTIONS, Icons.AutoMirrored.Filled.ReceiptLong, "nav_transactions"),
        NavItem("budgets_goals", Localization.Key.BUDGETS, Icons.Default.PieChart, "nav_budgets"),
        NavItem("debts", Localization.Key.DEBTS_LOANS, Icons.Default.People, "nav_debts"),
        NavItem("reports", Localization.Key.REPORTS, Icons.Default.BarChart, "nav_reports")
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 0.dp
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                val label = when (item.route) {
                    "dashboard" -> if (isBangla) "হোম" else "Home"
                    "transactions" -> if (isBangla) "লেনদেন" else "History"
                    "budgets_goals" -> if (isBangla) "বাজেট" else "Budgets"
                    "debts" -> if (isBangla) "দেনা-পাওনা" else "Debts"
                    "reports" -> if (isBangla) "রিপোর্ট" else "Reports"
                    else -> Localization.getString(item.titleKey, isBangla)
                }

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onNavigate(item.route) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = label
                        )
                    },
                    label = {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag(item.testTag)
                )
            }
        }
    }
}
