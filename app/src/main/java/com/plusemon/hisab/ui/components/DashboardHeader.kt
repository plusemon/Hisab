package com.plusemon.hisab.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plusemon.hisab.data.model.AppSyncStatus
import com.plusemon.hisab.data.model.User
import com.plusemon.hisab.data.model.UserSettings
import com.plusemon.hisab.domain.util.Formatters

/**
 * Compact Dashboard Header displayed ONLY on the root tabs (Home/Dashboard).
 * Features:
 * 1. Profile row: Avatar + Greeting & User Name on left;
 *    Lightweight IconButtons (24dp icon in 40dp touch target) for Theme, Language, and Settings on right.
 * 2. Subtle inline sync and date strip directly below profile row.
 * 3. Seamless statusBarsPadding with surface theme tokens.
 */
@Composable
fun DashboardHeader(
    user: User?,
    settings: UserSettings,
    syncStatus: AppSyncStatus = AppSyncStatus.Synced(),
    onSyncClick: () -> Unit = {},
    onToggleDarkMode: () -> Unit = {},
    onToggleLanguage: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = onProfileClick,
    modifier: Modifier = Modifier
) {
    val isBn = settings.language == "bn"

    val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when {
        currentHour in 5..11 -> if (isBn) "শুভ সকাল" else "Good Morning"
        currentHour in 12..16 -> if (isBn) "শুভ অপরাহ্ন" else "Good Afternoon"
        currentHour in 17..20 -> if (isBn) "শুভ সন্ধ্যা" else "Good Evening"
        else -> if (isBn) "শুভ রাত্রি" else "Good Night"
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // Main Top Bar Row: Profile on Left, Clean Sync/Notification Indicator on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side: User Avatar + Column (Greeting caption & User Name)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onProfileClick() }
                        .padding(vertical = 2.dp, horizontal = 2.dp)
                ) {
                    UserAvatar(
                        photoUrl = user?.photoUrl,
                        displayName = user?.displayName,
                        size = 38.dp,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        // Small subtext: Greeting (caption style, muted text color)
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                        // Primary text: User Name (TitleMedium / 16-18sp, SemiBold)
                        Text(
                            text = user?.displayName ?: (if (isBn) "স্বাগতম" else "Welcome"),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Right side: Clean real-time notification / sync indicator (Settings, Language & Theme moved to More tab)
                RealTimeStatusBadge(
                    status = syncStatus,
                    isBn = isBn,
                    onSyncClick = onSyncClick
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Subtle date line directly below
            val dateText = Formatters.formatDate(System.currentTimeMillis(), isBangla = isBn)
            Text(
                text = dateText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                modifier = Modifier.padding(start = 2.dp, top = 2.dp)
            )
        }
    }
}
