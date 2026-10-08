package com.plusemon.hisab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.plusemon.hisab.data.model.TransactionType
import com.plusemon.hisab.data.model.TransactionWithDetails
import com.plusemon.hisab.ui.components.HisabBottomNav
import com.plusemon.hisab.ui.components.HisabTopBar
import com.plusemon.hisab.ui.components.PinLockScreen
import com.plusemon.hisab.ui.components.UpdateDialog
import com.plusemon.hisab.ui.navigation.Screen
import com.plusemon.hisab.ui.screens.accounts.AccountsScreen
import com.plusemon.hisab.ui.screens.auth.AuthScreen
import com.plusemon.hisab.ui.screens.budgets.BudgetsAndGoalsScreen
import com.plusemon.hisab.ui.screens.dashboard.DashboardScreen
import com.plusemon.hisab.ui.screens.debts.DebtsScreen
import com.plusemon.hisab.ui.screens.recurring.RecurringScreen
import com.plusemon.hisab.ui.screens.reports.ReportsScreen
import com.plusemon.hisab.ui.screens.settings.SettingsScreen
import com.plusemon.hisab.ui.screens.transactions.AddEditTransactionScreen
import com.plusemon.hisab.ui.screens.transactions.TransactionsScreen
import com.plusemon.hisab.ui.theme.HisabTheme
import com.plusemon.hisab.ui.viewmodel.HisabViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: HisabViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsState()
            val currentUser by viewModel.currentUser.collectAsState()
            val isAuthInitializing by viewModel.isAuthInitializing.collectAsState()
            val isLocked by viewModel.isAppLocked.collectAsState()

            HisabTheme(darkTheme = settings.isDarkMode) {
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(Unit) {
                    viewModel.snackbarMessage.collectLatest { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                // Authentication Gate: Check loading first to prevent screen flicker
                if (isAuthInitializing && currentUser == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (currentUser == null) {
                    AuthScreen(viewModel = viewModel)
                } else if (isLocked) {
                    PinLockScreen(
                        isBangla = settings.language == "bn",
                        onPinEntered = { pin -> viewModel.verifyPin(pin) },
                        onForgotOrReset = { viewModel.signOut() }
                    )
                } else {
                    HisabMainApp(
                        viewModel = viewModel,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Auto-lock when backgrounded if PIN is configured
        viewModel.lockApp()
    }
}

@Composable
fun HisabMainApp(
    viewModel: HisabViewModel,
    snackbarHostState: SnackbarHostState
) {
    val settings by viewModel.settings.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val updateUiState by viewModel.updateUiState.collectAsState()
    val syncStatus by viewModel.appSyncStatus.collectAsState()
    val isBn = settings.language == "bn"

    val mainRoutes = remember {
        listOf(
            Screen.Dashboard.route,
            Screen.Transactions.route,
            Screen.BudgetsAndGoals.route,
            Screen.Debts.route,
            Screen.Reports.route
        )
    }

    val pagerState = rememberPagerState(initialPage = 0) { mainRoutes.size }
    val coroutineScope = rememberCoroutineScope()

    var currentSubscreen by remember { mutableStateOf<String?>(null) }
    var selectedTransactionForEdit by remember { mutableStateOf<TransactionWithDetails?>(null) }
    var initialTransactionType by remember { mutableStateOf(TransactionType.EXPENSE) }

    val isSubscreen = currentSubscreen != null
    val currentRoute = if (isSubscreen) currentSubscreen!! else mainRoutes[pagerState.currentPage]

    // Handle Back Press on Subscreens
    BackHandler(enabled = isSubscreen) {
        currentSubscreen = null
        selectedTransactionForEdit = null
    }

    // Handle Back Press on Main Tabs: Return to Dashboard if on another tab
    BackHandler(enabled = !isSubscreen && pagerState.currentPage != 0) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(0)
        }
    }

    // App-wide in-app update dialog so checking, downloading progress, and ready alerts appear anywhere
    UpdateDialog(
        updateUiState = updateUiState,
        isBangla = isBn,
        onDownloadAndInstall = { info -> viewModel.downloadAndInstallUpdate(info) },
        onDismiss = {
            val info = (updateUiState as? com.plusemon.hisab.ui.viewmodel.UpdateUiState.Available)?.updateInfo
            if (info != null) {
                viewModel.dismissUpdate(info.version)
            } else {
                viewModel.dismissUpdateState()
            }
        },
        onEnablePermission = { viewModel.openInstallPermissionSettings() },
        onRetryInstall = { info -> viewModel.retryInstall(info) }
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (!isSubscreen) {
                HisabTopBar(
                    user = currentUser,
                    settings = settings,
                    syncStatus = syncStatus,
                    onSyncClick = { viewModel.triggerManualSync() },
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onProfileClick = { currentSubscreen = Screen.Settings.route },
                    onSettingsClick = { currentSubscreen = Screen.Settings.route }
                )
            }
        },
        bottomBar = {
            if (!isSubscreen) {
                HisabBottomNav(
                    currentRoute = currentRoute,
                    isBangla = isBn,
                    onNavigate = { route ->
                        val targetIndex = mainRoutes.indexOf(route)
                        if (targetIndex >= 0) {
                            currentSubscreen = null
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(targetIndex)
                            }
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            if (currentSubscreen != null) {
                AnimatedContent(
                    targetState = currentSubscreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "SubscreenTransition"
                ) { subscreen ->
                    when (subscreen) {
                        Screen.AddEditTransaction.route -> {
                            AddEditTransactionScreen(
                                viewModel = viewModel,
                                existingTransaction = selectedTransactionForEdit,
                                initialType = initialTransactionType,
                                onNavigateBack = {
                                    currentSubscreen = null
                                    selectedTransactionForEdit = null
                                }
                            )
                        }

                        Screen.Accounts.route -> {
                            AccountsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentSubscreen = null }
                            )
                        }

                        Screen.Recurring.route -> {
                            RecurringScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentSubscreen = Screen.Settings.route }
                            )
                        }

                        Screen.Settings.route -> {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentSubscreen = null },
                                onNavigateToRecurring = { currentSubscreen = Screen.Recurring.route }
                            )
                        }
                    }
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1
                ) { page ->
                    when (page) {
                        0 -> {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToAddTransaction = { type ->
                                    initialTransactionType = type
                                    selectedTransactionForEdit = null
                                    currentSubscreen = Screen.AddEditTransaction.route
                                },
                                onNavigateToTransactions = {
                                    coroutineScope.launch { pagerState.animateScrollToPage(1) }
                                },
                                onNavigateToAccounts = { currentSubscreen = Screen.Accounts.route },
                                onNavigateToBudgets = {
                                    coroutineScope.launch { pagerState.animateScrollToPage(2) }
                                },
                                onNavigateToDebts = {
                                    coroutineScope.launch { pagerState.animateScrollToPage(3) }
                                },
                                onNavigateToSettings = { currentSubscreen = Screen.Settings.route },
                                onTransactionClick = { txItem ->
                                    selectedTransactionForEdit = txItem
                                    currentSubscreen = Screen.AddEditTransaction.route
                                }
                            )
                        }

                        1 -> {
                            TransactionsScreen(
                                viewModel = viewModel,
                                onNavigateToAddTransaction = {
                                    selectedTransactionForEdit = null
                                    initialTransactionType = TransactionType.EXPENSE
                                    currentSubscreen = Screen.AddEditTransaction.route
                                },
                                onTransactionClick = { txItem ->
                                    selectedTransactionForEdit = txItem
                                    currentSubscreen = Screen.AddEditTransaction.route
                                }
                            )
                        }

                        2 -> {
                            BudgetsAndGoalsScreen(
                                viewModel = viewModel
                            )
                        }

                        3 -> {
                            DebtsScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    coroutineScope.launch { pagerState.animateScrollToPage(0) }
                                }
                            )
                        }

                        4 -> {
                            ReportsScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
