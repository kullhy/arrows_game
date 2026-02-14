package com.batodev.arrows.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batodev.arrows.GameConstants
import com.batodev.arrows.R
import com.batodev.arrows.ui.theme.LocalThemeColors
import com.batodev.arrows.ui.theme.White

@Composable
fun IntroOverlay(onDismiss: () -> Unit) {
    val themeColors = LocalThemeColors.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = GameConstants.INTRO_OVERLAY_ALPHA))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { /* consume taps */ },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = themeColors.bottomBar,
                    shape = RoundedCornerShape(GameConstants.INTRO_CARD_CORNER_RADIUS.dp)
                )
                .padding(GameConstants.INTRO_CARD_PADDING.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.intro_instruction),
                color = White,
                fontSize = GameConstants.INTRO_INSTRUCTION_FONT_SIZE.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(GameConstants.INTRO_SPACING.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
            ) {
                Text(
                    text = stringResource(R.string.intro_got_it),
                    color = White,
                    fontSize = GameConstants.INTRO_BUTTON_FONT_SIZE.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
