package com.plusemon.hisab.ui.screens.budgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.plusemon.hisab.data.model.Category
import com.plusemon.hisab.data.model.SavingsGoal
import com.plusemon.hisab.data.model.TransactionType
import com.plusemon.hisab.domain.util.Formatters
import com.plusemon.hisab.domain.util.Localization
import com.plusemon.hisab.ui.components.CategoryIconBadge
import com.plusemon.hisab.ui.components.CurrencyAmountText
import com.plusemon.hisab.ui.components.DeleteConfirmationDialog
import com.plusemon.hisab.ui.components.EmptyStateView
import com.plusemon.hisab.ui.components.getIconByName
import com.plusemon.hisab.ui.components.parseColorHex
import com.plusemon.hisab.ui.theme.AmberTertiary
import com.plusemon.hisab.ui.theme.ExpenseRed
import com.plusemon.hisab.ui.theme.IncomeGreen
import com.plusemon.hisab.ui.viewmodel.BudgetStatus
import com.plusemon.hisab.ui.viewmodel.CategorySpendProgress
import com.plusemon.hisab.ui.viewmodel.HisabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsAndGoalsScreen(
    viewModel: HisabViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val isBn = settings.language == "bn"
    val useBnDigits = settings.numeralSystem == "bn"
    val hideBalances = settings.hideBalances
    val currSymbol = settings.currencySymbol

    val categoryProgressList by viewModel.categoryProgressList.collectAsState()
    val overallBudgetProgress by viewModel.overallBudgetProgress.collectAsState()
    val savingsGoals by viewModel.savingsGoals.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val budgets by viewModel.budgets.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Budgets, 1: Savings Goals

    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var adjustingGoal by remember { mutableStateOf<SavingsGoal?>(null) }
    var isDepositMode by remember { mutableStateOf(true) }
    var goalToDelete by remember { mutableStateOf<SavingsGoal?>(null) }
    var budgetToDelete by remember { mutableStateOf<CategorySpendProgress?>(null) }

    // Dialogs
    if (goalToDelete != null) {
        val target = goalToDelete!!
        val amountStr = Formatters.formatAmount(target.targetAmount, currSymbol, useBnDigits)
        DeleteConfirmationDialog(
            title = if (isBn) "সঞ্চয় লক্ষ্য মুছে ফেলবেন?" else "Delete Savings Goal?",
            message = if (isBn)
                "আপনি কি নিশ্চিত যে '${target.name}' সঞ্চয় লক্ষ্যটি মুছে ফেলতে চান?"
            else
                "Are you sure you want to delete the savings goal '${target.name}'?",
            itemDetail = "${target.name} • ${if (isBn) "লক্ষ্যমাত্রা:" else "Target:"} $amountStr",
            isBangla = isBn,
            onConfirm = {
                viewModel.deleteSavingsGoal(target.id)
                goalToDelete = null
            },
            onDismiss = {
                goalToDelete = null
            }
        )
    }

    if (budgetToDelete != null) {
        val target = budgetToDelete!!
        val isOverall = target.category.id == 0L || target.budgetLimit == overallBudgetProgress?.budgetLimit && target.category.nameEn == "Overall Budget"
        val matchedBudget = if (isOverall) {
            budgets.firstOrNull { it.categoryId == null }
        } else {
            budgets.firstOrNull { it.categoryId == target.category.id }
        }
        val budgetTitle = if (isOverall) (if (isBn) "মোট বাজেট" else "Overall Budget") else target.category.localizedName(isBn)
        val limitStr = Formatters.formatAmount(target.budgetLimit ?: 0.0, currSymbol, useBnDigits)

        DeleteConfirmationDialog(
            title = if (isBn) "বাজেট মুছে ফেলবেন?" else "Delete Budget?",
            message = if (isBn)
                "আপনি কি নিশ্চিত যে '$budgetTitle'-এর বাজেট মুছে ফেলতে চান?"
            else
                "Are you sure you want to delete the budget for '$budgetTitle'?",
            itemDetail = "$budgetTitle • ${if (isBn) "সীমা:" else "Limit:"} $limitStr",
            isBangla = isBn,
            onConfirm = {
                if (matchedBudget != null) {
                    viewModel.deleteBudget(matchedBudget.id)
                }
                budgetToDelete = null
            },
            onDismiss = {
                budgetToDelete = null
            }
        )
    }

    if (showAddBudgetDialog) {
        AddEditBudgetDialog(
            categories = categories.filter { it.type == TransactionType.EXPENSE },
            isBangla = isBn,
            onDismiss = { showAddBudgetDialog = false },
            onSave = { categoryId, limit ->
                viewModel.saveBudget(categoryId, limit)
                showAddBudgetDialog = false
            }
        )
    }

    if (showAddGoalDialog) {
        AddSavingsGoalDialog(
            isBangla = isBn,
            onDismiss = { showAddGoalDialog = false },
            onSave = { name, targetAmount, targetDate, note, colorHex, iconName ->
                viewModel.addSavingsGoal(name, targetAmount, targetDate, note, colorHex, iconName)
                showAddGoalDialog = false
            }
        )
    }

    if (adjustingGoal != null) {
        AdjustGoalSavingsDialog(
            goal = adjustingGoal!!,
            isDeposit = isDepositMode,
            isBangla = isBn,
            currencySymbol = currSymbol,
            onDismiss = { adjustingGoal = null },
            onConfirm = { amount ->
                val delta = if (isDepositMode) amount else -amount
                viewModel.adjustGoalSavings(adjustingGoal!!.id, delta)
                adjustingGoal = null
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showAddBudgetDialog = true else showAddGoalDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("budgets_goals_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Header (Budgets vs Savings Goals)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = Localization.getString(Localization.Key.BUDGETS, isBn),
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_budgets")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = Localization.getString(Localization.Key.SAVINGS_GOALS, isBn),
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_goals")
                )
            }

            if (selectedTab == 0) {
                // BUDGETS TAB
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Overall Budget Card (if configured)
                    if (overallBudgetProgress != null) {
                        item {
                            OverallBudgetCard(
                                progress = overallBudgetProgress!!,
                                isBangla = isBn,
                                useBnDigits = useBnDigits,
                                hideBalances = hideBalances,
                                currSymbol = currSymbol,
                                onDelete = { budgetToDelete = overallBudgetProgress }
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBn) "ক্যাটাগরি অনুযায়ী বাজেট" else "Category Budgets",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    val budgetedCategories = categoryProgressList.filter { it.budgetLimit != null && it.budgetLimit > 0 }
                    if (budgetedCategories.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.PieChart,
                                title = Localization.getString(Localization.Key.EMPTY_BUDGETS, isBn),
                                description = if (isBn) "মাসিক বাজেট নির্ধারণ করতে নিচের + বাটনে চাপ দিন।" else "Tap the + button below to set up your monthly budget.",
                                actionLabel = if (isBn) "+ বাজেট সেট করুন" else "+ Set Budget",
                                onActionClick = { showAddBudgetDialog = true }
                            )
                        }
                    } else {
                        items(budgetedCategories, key = { it.category.id }) { item ->
                            CategoryBudgetProgressItem(
                                progress = item,
                                isBangla = isBn,
                                useBnDigits = useBnDigits,
                                hideBalances = hideBalances,
                                currSymbol = currSymbol,
                                onDelete = { budgetToDelete = item }
                            )
                        }
                    }
                }
            } else {
                // SAVINGS GOALS TAB
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (savingsGoals.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.Savings,
                                title = Localization.getString(Localization.Key.EMPTY_GOALS, isBn),
                                description = if (isBn) "সঞ্চয় লক্ষ্য সেট করতে নিচের + বাটনে চাপ দিন।" else "Tap the + button below to create a savings goal.",
                                actionLabel = if (isBn) "+ সঞ্চয় লক্ষ্য যোগ করুন" else "+ Create Savings Goal",
                                onActionClick = { showAddGoalDialog = true }
                            )
                        }
                    } else {
                        items(savingsGoals, key = { it.id }) { goal ->
                            SavingsGoalItemCard(
                                goal = goal,
                                isBangla = isBn,
                                useBnDigits = useBnDigits,
                                hideBalances = hideBalances,
                                currSymbol = currSymbol,
                                onDeposit = {
                                    adjustingGoal = goal
                                    isDepositMode = true
                                },
                                onWithdraw = {
                                    adjustingGoal = goal
                                    isDepositMode = false
                                },
                                onDelete = { goalToDelete = goal }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OverallBudgetCard(
    progress: CategorySpendProgress,
    isBangla: Boolean,
    useBnDigits: Boolean,
    hideBalances: Boolean,
    currSymbol: String,
    onDelete: (() -> Unit)? = null
) {
    val statusColor = when (progress.status) {
        BudgetStatus.EXCEEDED -> ExpenseRed
        BudgetStatus.WARNING -> AmberTertiary
        else -> IncomeGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Localization.getString(Localization.Key.OVERALL_BUDGET, isBangla),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = when (progress.status) {
                                BudgetStatus.EXCEEDED -> Localization.getString(Localization.Key.BUDGET_EXCEEDED, isBangla)
                                BudgetStatus.WARNING -> Localization.getString(Localization.Key.BUDGET_WARNING, isBangla)
                                else -> Localization.getString(Localization.Key.BUDGET_SAFE, isBangla)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (onDelete != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Budget",
                                tint = ExpenseRed.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { progress.percentage.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = Localization.getString(Localization.Key.SPENT, isBangla),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    CurrencyAmountText(
                        amount = progress.spentAmount,
                        currencySymbol = currSymbol,
                        useBanglaDigits = useBnDigits,
                        hideBalances = hideBalances,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Localization.getString(Localization.Key.LIMIT, isBangla),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    CurrencyAmountText(
                        amount = progress.budgetLimit ?: 0.0,
                        currencySymbol = currSymbol,
                        useBanglaDigits = useBnDigits,
                        hideBalances = hideBalances,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryBudgetProgressItem(
    progress: CategorySpendProgress,
    isBangla: Boolean,
    useBnDigits: Boolean,
    hideBalances: Boolean,
    currSymbol: String,
    onDelete: (() -> Unit)? = null
) {
    val statusColor = when (progress.status) {
        BudgetStatus.EXCEEDED -> ExpenseRed
        BudgetStatus.WARNING -> AmberTertiary
        else -> IncomeGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryIconBadge(
                    iconName = progress.category.iconName,
                    colorHex = progress.category.colorHex,
                    size = 40.dp,
                    iconSize = 20.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = progress.category.localizedName(isBangla),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    val remaining = ((progress.budgetLimit ?: 0.0) - progress.spentAmount).coerceAtLeast(0.0)
                    Text(
                        text = "${Localization.getString(Localization.Key.REMAINING, isBangla)}: ${Formatters.formatAmount(remaining, currSymbol, useBnDigits, hideBalances)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${(progress.percentage * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )

                    if (onDelete != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Budget",
                                tint = ExpenseRed.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress.percentage.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun SavingsGoalItemCard(
    goal: SavingsGoal,
    isBangla: Boolean,
    useBnDigits: Boolean,
    hideBalances: Boolean,
    currSymbol: String,
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit,
    onDelete: () -> Unit
) {
    val progressPercent = if (goal.targetAmount > 0) (goal.currentSavedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val goalColor = parseColorHex(goal.colorHex)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconByName(goal.iconName),
                        contentDescription = null,
                        tint = goalColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (goal.targetDate != null) {
                        Text(
                            text = "${Localization.getString(Localization.Key.TARGET_DATE, isBangla)}: ${Formatters.formatDate(goal.targetDate, isBangla)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = Localization.getString(Localization.Key.CURRENT_SAVED, isBangla),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    CurrencyAmountText(
                        amount = goal.currentSavedAmount,
                        currencySymbol = currSymbol,
                        useBanglaDigits = useBnDigits,
                        hideBalances = hideBalances,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = goalColor
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Localization.getString(Localization.Key.TARGET_AMOUNT, isBangla),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    CurrencyAmountText(
                        amount = goal.targetAmount,
                        currencySymbol = currSymbol,
                        useBanglaDigits = useBnDigits,
                        hideBalances = hideBalances,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = goalColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onWithdraw,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(Localization.getString(Localization.Key.WITHDRAW, isBangla))
                }

                Button(
                    onClick = onDeposit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = goalColor)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Localization.getString(Localization.Key.DEPOSIT, isBangla), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetDialog(
    categories: List<Category>,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (categoryId: Long?, limit: Double) -> Unit
) {
    var isOverall by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(categories.firstOrNull()?.id) }
    var limitText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(
                    text = Localization.getString(Localization.Key.MONTHLY_BUDGET, isBangla),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isOverall = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isOverall) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (!isOverall) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isBangla) "ক্যাটাগরি বাজেট" else "Category Budget", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { isOverall = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOverall) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isOverall) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isBangla) "মোট বাজেট" else "Overall Budget", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!isOverall && categories.isNotEmpty()) {
                    Text(
                        text = Localization.getString(Localization.Key.CATEGORY, isBangla),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    var expanded by remember { mutableStateOf(false) }
                    val currentCat = categories.firstOrNull { it.id == selectedCategoryId }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = currentCat?.localizedName(isBangla) ?: "",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.localizedName(isBangla)) },
                                    onClick = {
                                        selectedCategoryId = cat.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text(Localization.getString(Localization.Key.LIMIT, isBangla)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
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
                            val limit = limitText.toDoubleOrNull() ?: 0.0
                            if (limit > 0) {
                                onSave(if (isOverall) null else selectedCategoryId, limit)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(Localization.getString(Localization.Key.SAVE, isBangla), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddSavingsGoalDialog(
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, targetAmount: Double, targetDate: Long?, note: String, colorHex: String, iconName: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetAmountText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(
                    text = Localization.getString(Localization.Key.SAVINGS_GOALS, isBangla),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Localization.getString(Localization.Key.GOAL_NAME, isBangla)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { targetAmountText = it },
                    label = { Text(Localization.getString(Localization.Key.TARGET_AMOUNT, isBangla)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
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
                            val target = targetAmountText.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank() && target > 0) {
                                onSave(name.trim(), target, null, "", "#0D9488", "savings")
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(Localization.getString(Localization.Key.SAVE, isBangla), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdjustGoalSavingsDialog(
    goal: SavingsGoal,
    isDeposit: Boolean,
    isBangla: Boolean,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(
                    text = if (isDeposit) "${Localization.getString(Localization.Key.DEPOSIT, isBangla)} (${goal.name})"
                    else "${Localization.getString(Localization.Key.WITHDRAW, isBangla)} (${goal.name})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(Localization.getString(Localization.Key.AMOUNT, isBangla)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
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
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                onConfirm(amt)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(Localization.getString(Localization.Key.SAVE, isBangla), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
