package com.batodev.arrows.ui

import android.content.Intent
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.batodev.arrows.GameConstants
import com.batodev.arrows.GenerateActivity
import com.batodev.arrows.MainActivity
import com.batodev.arrows.R
import com.batodev.arrows.SettingsActivity
import com.batodev.arrows.ui.theme.InactiveIcon
import com.batodev.arrows.ui.theme.NavigationIndicator
import com.batodev.arrows.ui.theme.ThemeColors
import com.batodev.arrows.ui.theme.White

@Composable
fun AppNavigationBar(
    selectedDestination: NavigationDestination,
    levelNumber: Int,
    themeColors: ThemeColors
) {
    val isGeneratorUnlocked = levelNumber >= GameConstants.GENERATOR_UNLOCK_LEVEL
    val context = LocalContext.current

    NavigationBar(
        containerColor = themeColors.bottomBar,
        contentColor = White
    ) {
        GeneratorNavigationItem(
            isUnlocked = isGeneratorUnlocked,
            selected = selectedDestination == NavigationDestination.GENERATOR,
            context = context
        )
        HomeNavigationItem(
            selected = selectedDestination == NavigationDestination.HOME,
            context = context
        )
        SettingsNavigationItem(
            selected = selectedDestination == NavigationDestination.SETTINGS,
            context = context
        )
    }
}

@Composable
fun RowScope.HomeNavigationItem(selected: Boolean, context: android.content.Context) {
    NavigationBarItem(
        icon = {
            Icon(
                Icons.Default.Home,
                contentDescription = stringResource(R.string.home_label)
            )
        },
        label = { Text(stringResource(R.string.home_label)) },
        selected = selected,
        onClick = {
            if (!selected) {
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                context.startActivity(intent)
            }
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = White,
            indicatorColor = NavigationIndicator,
            selectedTextColor = White,
            unselectedIconColor = InactiveIcon,
            unselectedTextColor = InactiveIcon
        )
    )
}

@Composable
fun RowScope.SettingsNavigationItem(selected: Boolean, context: android.content.Context) {
    NavigationBarItem(
        icon = {
            Icon(
                Icons.Default.Settings,
                contentDescription = stringResource(R.string.settings_label)
            )
        },
        label = { Text(stringResource(R.string.settings_label)) },
        selected = selected,
        onClick = {
            if (!selected) {
                val intent = Intent(context, SettingsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                context.startActivity(intent)
            }
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = White,
            indicatorColor = NavigationIndicator,
            selectedTextColor = White,
            unselectedIconColor = InactiveIcon,
            unselectedTextColor = InactiveIcon
        )
    )
}

@Composable
fun RowScope.GeneratorNavigationItem(isUnlocked: Boolean, selected: Boolean = false, context: android.content.Context) {
    val icon = if (isUnlocked) Icons.Default.AutoAwesome else Icons.Default.Lock
    val label = if (isUnlocked) stringResource(R.string.custom_gen_title)
    else stringResource(R.string.level_label, GameConstants.GENERATOR_UNLOCK_LEVEL)

    NavigationBarItem(
        icon = { Icon(icon, contentDescription = stringResource(R.string.content_description_generate)) },
        label = { Text(label) },
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
