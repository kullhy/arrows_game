package com.batodev.arrows.ui.game

import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoLabel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batodev.arrows.ui.theme.HeartRed
import com.batodev.arrows.ui.theme.ProgressBarGreen
import com.batodev.arrows.ui.theme.White

@Composable
fun GameTopBar(
    lives: Int,
    maxLives: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit
) {
    val themeColors = com.batodev.arrows.ui.theme.LocalThemeColors.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Controls
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                colors = IconButtonDefaults.iconButtonColors(containerColor = themeColors.topBarButton),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onRestart,
                colors = IconButtonDefaults.iconButtonColors(containerColor = themeColors.topBarButton),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Restart",
                    tint = White
                )
            }
        }

        // Hearts
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(maxLives) { index ->
                Icon(
                    imageVector = if (index < lives) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Life",
                    tint = HeartRed,
                    modifier = Modifier.size(24.dp)
                )
                if (index < maxLives - 1) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }

        // Right Control (Loading/Hint)
        Button(
            onClick = { /* TODO */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = themeColors.topBarButton, contentColor = White
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier.height(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.VideoLabel,
                contentDescription = "Ad",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Loading..", fontSize = 12.sp)
        }
    }
}

@Composable
fun GameProgressBar(
    totalSnakes: Int,
    currentSnakes: Int
) {
    val themeColors = com.batodev.arrows.ui.theme.LocalThemeColors.current
    
    val targetProgress = if (totalSnakes > 0) {
        (totalSnakes - currentSnakes).toFloat() / totalSnakes
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 500),
        label = "ProgressBarAnimation"
    )

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp),
        color = ProgressBarGreen,
        trackColor = themeColors.topBarButton,
        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
    )
}
