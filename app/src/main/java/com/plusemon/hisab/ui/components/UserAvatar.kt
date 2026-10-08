package com.plusemon.hisab.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

/**
 * Modern user avatar that displays user profile image from URL (e.g. Google Sign-In)
 * with graceful fallback to monogram initials.
 */
@Composable
fun UserAvatar(
    photoUrl: String?,
    displayName: String?,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    shape: Shape = CircleShape,
    border: BorderStroke? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    val initials = remember(displayName) {
        val name = displayName?.trim().orEmpty()
        if (name.isEmpty()) {
            "H"
        } else {
            val parts = name.split("\\s+".toRegex()).filter { it.isNotBlank() }
            if (parts.size >= 2) {
                "${parts[0].first()}${parts[1].first()}".uppercase()
            } else {
                name.take(2).uppercase()
            }
        }
    }

    val boxModifier = modifier
        .size(size)
        .then(if (border != null) Modifier.border(border, shape) else Modifier)
        .clip(shape)

    if (!photoUrl.isNullOrBlank()) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photoUrl)
                .crossfade(true)
                .build(),
            contentDescription = displayName ?: "User Profile Picture",
            contentScale = ContentScale.Crop,
            modifier = boxModifier,
            loading = {
                FallbackAvatarContent(
                    initials = initials,
                    size = size,
                    backgroundColor = backgroundColor,
                    contentColor = contentColor
                )
            },
            error = {
                FallbackAvatarContent(
                    initials = initials,
                    size = size,
                    backgroundColor = backgroundColor,
                    contentColor = contentColor
                )
            }
        )
    } else {
        Box(
            modifier = boxModifier.background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            FallbackAvatarContent(
                initials = initials,
                size = size,
                backgroundColor = backgroundColor,
                contentColor = contentColor
            )
        }
    }
}

@Composable
private fun FallbackAvatarContent(
    initials: String,
    size: Dp,
    backgroundColor: Color,
    contentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        val fontSize = (size.value * 0.40f).coerceIn(11f, 26f).sp
        Text(
            text = initials,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            fontSize = fontSize,
            maxLines = 1,
            softWrap = false
        )
    }
}
