package com.plusemon.hisab.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusemon.hisab.data.model.AppSyncStatus
import com.plusemon.hisab.data.model.User
import com.plusemon.hisab.data.model.UserSettings

/**
 * Backward compatibility alias for [DashboardHeader].
 */
@Composable
fun HisabTopBar(
    user: User?,
    settings: UserSettings,
    syncStatus: AppSyncStatus = AppSyncStatus.Synced(),
    onSyncClick: () -> Unit = {},
    onToggleDarkMode: () -> Unit,
    onToggleLanguage: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit = onProfileClick,
    modifier: Modifier = Modifier
) {
    DashboardHeader(
        user = user,
        settings = settings,
        syncStatus = syncStatus,
        onSyncClick = onSyncClick,
        onToggleDarkMode = onToggleDarkMode,
        onToggleLanguage = onToggleLanguage,
        onProfileClick = onProfileClick,
        onSettingsClick = onSettingsClick,
        modifier = modifier
    )
}
