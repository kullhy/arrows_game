package com.batodev.arrows.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Grid4x4
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batodev.arrows.GameConstants
import com.batodev.arrows.GenerateActivity
import com.batodev.arrows.MainActivity
import com.batodev.arrows.R
import com.batodev.arrows.ui.theme.InactiveIcon
import com.batodev.arrows.ui.theme.LocalThemeColors
import com.batodev.arrows.ui.theme.NavigationIndicator
import com.batodev.arrows.ui.theme.ThemeColors
import com.batodev.arrows.ui.theme.White

@Composable
fun SettingsBottomBar(context: Context, themeColors: ThemeColors, levelNumber: Int = 1) {
    val isGeneratorUnlocked = levelNumber >= GameConstants.GENERATOR_UNLOCK_LEVEL

    NavigationBar(containerColor = themeColors.bottomBar, contentColor = White) {
        GeneratorNavigationItem(isGeneratorUnlocked, selected = false)
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = stringResource(R.string.home_label)
                )
            },
            label = { Text(stringResource(R.string.home_label)) },
            selected = false,
            onClick = {
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                context.startActivity(intent)
            },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = InactiveIcon,
                unselectedTextColor = InactiveIcon
            )
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings_label)
                )
            },
            label = { Text(stringResource(R.string.settings_label)) },
            selected = true,
            onClick = { },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = White,
                indicatorColor = NavigationIndicator,
                selectedTextColor = White
            )
        )
    }
}

@Composable
fun RowScope.GeneratorNavigationItem(isUnlocked: Boolean, selected: Boolean = false) {
    val context = LocalContext.current
    NavigationBarItem(
        icon = {
            Icon(
                imageVector = if (isUnlocked) Icons.Default.AutoAwesome else Icons.Default.Lock,
                contentDescription = stringResource(R.string.content_description_generate)
            )
        },
        label = {
            Text(
                if (isUnlocked) stringResource(R.string.custom_gen_title)
                else stringResource(R.string.level_label, GameConstants.GENERATOR_UNLOCK_LEVEL)
            )
        },
        selected = selected,
        onClick = {
            if (isUnlocked) {
                val intent = Intent(context, GenerateActivity::class.java)
                context.startActivity(intent)
            }
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = White,
            unselectedIconColor = InactiveIcon,
            selectedTextColor = White,
            unselectedTextColor = InactiveIcon,
            indicatorColor = NavigationIndicator
        )
    )
}

data class PreferencesParams(
    val viewModel: AppViewModel,
    val themeColors: ThemeColors,
    val currentTheme: String,
    val currentSpeed: String,
    val onThemeClick: () -> Unit,
    val onSpeedClick: () -> Unit
)

@Composable
fun PreferencesSection(params: PreferencesParams) {
    val isVibrationEnabled by params.viewModel.isVibrationEnabled.collectAsState()
    val isSoundsEnabled by params.viewModel.isSoundsEnabled.collectAsState()
    val isFillBoardEnabled by params.viewModel.isFillBoardEnabled.collectAsState()

    SettingsGroup(params.themeColors.topBarButton) {
        SettingsSwitchItem(
            Icons.Default.Vibration, stringResource(R.string.vibrations_label),
            isVibrationEnabled, params.themeColors.accent
        ) { params.viewModel.saveVibration(it) }
        SettingsSwitchItem(
            Icons.AutoMirrored.Filled.VolumeUp, stringResource(R.string.sounds_label),
            isSoundsEnabled, params.themeColors.accent
        ) { params.viewModel.saveSounds(it) }
        SettingsSwitchItem(
            Icons.Default.Grid4x4, stringResource(R.string.fill_board_label),
            isFillBoardEnabled, params.themeColors.accent
        ) { params.viewModel.saveFillBoard(it) }
        SettingsClickableItem(
            Icons.Default.Palette, stringResource(R.string.theme_label),
            getLocalizedThemeName(params.currentTheme), params.onThemeClick
        )
        SettingsClickableItem(
            Icons.Default.Speed, stringResource(R.string.animation_speed_label),
            getLocalizedSpeedName(params.currentSpeed), params.onSpeedClick
        )
    }
}

@Composable
private fun getLocalizedThemeName(theme: String): String {
    return when (theme) {
        "Dark" -> stringResource(R.string.theme_dark)
        "Green" -> stringResource(R.string.theme_green)
        "Red" -> stringResource(R.string.theme_red)
        "Yellow" -> stringResource(R.string.theme_yellow)
        "Orange" -> stringResource(R.string.theme_orange)
        "Black and White" -> stringResource(R.string.theme_bw)
        else -> theme
    }
}

@Composable
private fun getLocalizedSpeedName(speed: String): String {
    return when (speed) {
        "High" -> stringResource(R.string.speed_high)
        "Medium" -> stringResource(R.string.speed_medium)
        "Low" -> stringResource(R.string.speed_low)
        else -> speed
    }
}

@Composable
fun FeedbackSection(context: Context, themeColors: ThemeColors) {
    SettingsGroup(themeColors.topBarButton) {
        SettingsClickableItem(Icons.Default.Star, stringResource(R.string.rate_us_label)) {
            SettingsUtils.launchReviewFlow(context)
        }
        SettingsClickableItem(Icons.Default.Edit, stringResource(R.string.write_us_label)) {
            SettingsUtils.launchEmail(context)
        }
        SettingsClickableItem(Icons.Default.Apps, stringResource(R.string.more_games_label)) {
            SettingsUtils.launchBrowser(
                context,
                "https://play.google.com/store/apps/dev?id=8228670503574649511"
            )
        }
    }
}

@Composable
fun PurchasesSection(themeColors: ThemeColors) {
    SettingsGroup(themeColors.topBarButton) {
        SettingsSwitchItem(
            Icons.Default.Block, stringResource(R.string.remove_ads_label),
            false, themeColors.accent
        )
    }
}

@Composable
fun LegalSection(context: Context, themeColors: ThemeColors) {
    SettingsGroup(themeColors.topBarButton) {
        SettingsClickableItem(Icons.Default.Description, stringResource(R.string.privacy_label)) {
            SettingsUtils.launchBrowser(context, "https://robmat.github.io/privacy_policy.html")
        }
    }
}

@Composable
fun ThemeSelectionDialog(currentTheme: String, onDismiss: () -> Unit, onThemeSelected: (String) -> Unit) {
    val themeColors = LocalThemeColors.current
    val themes = listOf("Dark", "Green", "Red", "Yellow", "Orange", "Black and White")
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = themeColors.bottomBar,
        title = {
            Text(
                text = stringResource(R.string.choose_theme_title),
                color = White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                themes.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = theme == currentTheme, onClick = { onThemeSelected(theme) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = themeColors.accent, unselectedColor = InactiveIcon
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = getLocalizedThemeName(theme), color = White, fontSize = 16.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_label), color = themeColors.accent)
            }
        }
    )
}

@Composable
fun AnimationSpeedSelectionDialog(currentSpeed: String, onDismiss: () -> Unit, onSpeedSelected: (String) -> Unit) {
    val themeColors = LocalThemeColors.current
    val speeds = listOf("High", "Medium", "Low")
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = themeColors.bottomBar,
        title = {
            Text(
                text = stringResource(R.string.animation_speed_label),
                color = White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                speeds.forEach { speed ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSpeedSelected(speed) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = speed == currentSpeed, onClick = { onSpeedSelected(speed) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = themeColors.accent, unselectedColor = InactiveIcon
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = getLocalizedSpeedName(speed), color = White, fontSize = 16.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_label), color = themeColors.accent)
            }
        }
    )
}
