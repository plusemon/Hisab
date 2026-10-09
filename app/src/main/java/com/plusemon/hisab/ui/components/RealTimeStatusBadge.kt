package com.plusemon.hisab.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plusemon.hisab.data.model.AppSyncStatus

private data class BadgeVisuals(
    val bgColor: Color,
    val contentColor: Color,
    val icon: ImageVector,
    val label: String,
    val isLoading: Boolean
)

@Composable
fun RealTimeStatusBadge(
    status: AppSyncStatus,
    isBn: Boolean,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val visuals = when (status) {
        is AppSyncStatus.Saving -> BadgeVisuals(
            bgColor = Color(0xFFFEF3C7),
            contentColor = Color(0xFFB45309),
            icon = Icons.Default.Save,
            label = if (isBn) "সংরক্ষণ হচ্ছে..." else "Saving...",
            isLoading = true
        )
        is AppSyncStatus.Saved -> BadgeVisuals(
            bgColor = Color(0xFFDCFCE7),
            contentColor = Color(0xFF15803D),
            icon = Icons.Default.Check,
            label = if (isBn) "সংরক্ষিত" else "Saved",
            isLoading = false
        )
        is AppSyncStatus.Syncing -> BadgeVisuals(
            bgColor = Color(0xFFE0E7FF),
            contentColor = Color(0xFF4338CA),
            icon = Icons.Default.CloudSync,
            label = if (isBn) "সিঙ্ক হচ্ছে..." else "Syncing...",
            isLoading = true
        )
        is AppSyncStatus.Synced -> BadgeVisuals(
            bgColor = Color(0xFFECFDF5),
            contentColor = Color(0xFF047857),
            icon = Icons.Default.CloudDone,
            label = if (isBn) "সিঙ্ক হয়েছে" else "Synced",
            isLoading = false
        )
        is AppSyncStatus.CheckingUpdates -> BadgeVisuals(
            bgColor = Color(0xFFF3E8FF),
            contentColor = Color(0xFF7E22CE),
            icon = Icons.Default.Refresh,
            label = if (isBn) "আপডেট খোঁজা হচ্ছে..." else "Checking updates...",
            isLoading = true
        )
        is AppSyncStatus.Error -> BadgeVisuals(
            bgColor = Color(0xFFFEE2E2),
            contentColor = Color(0xFFB91C1C),
            icon = Icons.Default.ErrorOutline,
            label = if (isBn) "সিঙ্ক ত্রুটি" else "Sync error",
            isLoading = false
        )
        is AppSyncStatus.Idle -> BadgeVisuals(
            bgColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            icon = Icons.Default.CloudDone,
            label = if (isBn) "সিঙ্ক হয়েছে" else "Synced",
            isLoading = false
        )
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onSyncClick)
            .testTag("realtime_status_badge"),
        color = visuals.bgColor,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 0.dp
    ) {
        AnimatedContent(
            targetState = visuals,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "StatusBadgeAnim"
        ) { currentVisuals ->
            val showText = !compact || currentVisuals.isLoading || status is AppSyncStatus.Error
            Row(
                modifier = Modifier.padding(
                    horizontal = if (showText) 10.dp else 6.dp,
                    vertical = 4.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentVisuals.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(11.dp),
                        strokeWidth = 1.6.dp,
                        color = currentVisuals.contentColor
                    )
                } else {
                    Icon(
                        imageVector = currentVisuals.icon,
                        contentDescription = currentVisuals.label,
                        tint = currentVisuals.contentColor,
                        modifier = Modifier.size(13.dp)
                    )
                }

                if (showText) {
                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = currentVisuals.label,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = currentVisuals.contentColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
