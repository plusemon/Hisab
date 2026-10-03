package com.plusemon.hisab.ui.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Dashboard : Screen("dashboard")
    object Transactions : Screen("transactions")
    object AddEditTransaction : Screen("add_edit_transaction")
    object Accounts : Screen("accounts")
    object AddEditAccount : Screen("add_edit_account")
    object BudgetsAndGoals : Screen("budgets_goals")
    object Debts : Screen("debts")
    object Reports : Screen("reports")
    object Recurring : Screen("recurring")
    object Settings : Screen("settings")
}
