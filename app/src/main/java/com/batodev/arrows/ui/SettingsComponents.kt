package com.batodev.arrows.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Apps
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batodev.arrows.MainActivity
import com.batodev.arrows.ui.theme.InactiveIcon
import com.batodev.arrows.ui.theme.LocalThemeColors
import com.batodev.arrows.ui.theme.NavigationIndicator
import com.batodev.arrows.ui.theme.ThemeColors
import com.batodev.arrows.ui.theme.White

@Composable
fun SettingsBottomBar(context: Context, themeColors: ThemeColors) {
    NavigationBar(containerColor = themeColors.bottomBar, contentColor = White) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Lock, contentDescription = "Levels") },
            label = { Text("Level 20") },
            selected = false,
            onClick = { },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = InactiveIcon,
                unselectedTextColor = InactiveIcon
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
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
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
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
            Icons.Default.Vibration, "Vibrations",
            isVibrationEnabled, params.themeColors.accent
        ) { params.viewModel.saveVibration(it) }
        SettingsSwitchItem(
            Icons.AutoMirrored.Filled.VolumeUp, "Sounds",
            isSoundsEnabled, params.themeColors.accent
        ) { params.viewModel.saveSounds(it) }
        SettingsSwitchItem(
            Icons.Default.Grid4x4, "Fill board (slower)",
            isFillBoardEnabled, params.themeColors.accent
        ) { params.viewModel.saveFillBoard(it) }
        SettingsClickableItem(
            Icons.Default.Palette, "Theme",
            params.currentTheme, params.onThemeClick
        )
        SettingsClickableItem(
            Icons.Default.Speed, "Animation Speed",
            params.currentSpeed, params.onSpeedClick
        )
    }
}

@Composable
fun FeedbackSection(context: Context, themeColors: ThemeColors) {
    SettingsGroup(themeColors.topBarButton) {
        SettingsClickableItem(Icons.Default.Star, "Rate us") {
            SettingsUtils.launchReviewFlow(context)
        }
        SettingsClickableItem(Icons.Default.Edit, "Write us") {
            SettingsUtils.launchEmail(context)
        }
        SettingsClickableItem(Icons.Default.Apps, "More Games") {
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
        SettingsSwitchItem(Icons.Default.Block, "Remove Ads", false, themeColors.accent)
    }
}

@Composable
fun LegalSection(context: Context, themeColors: ThemeColors) {
    SettingsGroup(themeColors.topBarButton) {
        SettingsClickableItem(Icons.Default.Description, "Privacy") {
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
        title = { Text(text = "Choose Theme", color = White, fontWeight = FontWeight.Bold) },
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
                        Text(text = theme, color = White, fontSize = 16.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = themeColors.accent) }
        }
    )
}

@Composable
fun AnimationSpeedSelectionDialog(currentSpeed: String, onDismiss: () -> Unit, onSpeedSelected: (String) -> Unit) {
    val themeColors = LocalThemeColors.current
    val speeds = listOf("High", "Medium", "Low")
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = themeColors.bottomBar,
        title = { Text(text = "Animation Speed", color = White, fontWeight = FontWeight.Bold) },
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
                        Text(text = speed, color = White, fontSize = 16.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = themeColors.accent) }
        }
    )
}
