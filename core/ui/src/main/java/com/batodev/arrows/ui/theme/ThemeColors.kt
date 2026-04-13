package com.batodev.arrows.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ThemeColors(
    val background: Color,
    val accent: Color,
    val snake: Color,
    val topBarButton: Color,
    val bottomBar: Color
)

val LocalThemeColors = staticCompositionLocalOf {
    ThemeColors(
        background = DarkBackground,
        accent = AccentBlue,
        snake = SnakeColor,
        topBarButton = TopBarButtonBackground,
        bottomBar = BottomBarBackground
    )
}

private val PuzzleColorScheme = lightColorScheme(
    primary = AccentBlue,
    secondary = BottomBarBackground,
    tertiary = LightCyan,
    background = DarkBackground,
    surface = TopBarButtonBackground,
    surfaceVariant = BottomBarBackground,
    onPrimary = White,
    onSecondary = White,
    onBackground = White,
    onSurface = White,
    outline = InactiveIcon
)

private fun getThemeColors(themeName: String): ThemeColors = when (themeName) {
    "Green" -> ThemeColors(
        GreenBackground, GreenAccent, GreenSnake,
        GreenAccent.copy(alpha = 0.2f), GreenBackground.copy(alpha = 0.8f)
    )
    "Red" -> ThemeColors(
        RedBackground, RedAccent, RedSnake,
        RedAccent.copy(alpha = 0.2f), RedBackground.copy(alpha = 0.8f)
    )
    "Yellow" -> ThemeColors(
        YellowBackground, YellowAccent, YellowSnake,
        YellowAccent.copy(alpha = 0.2f), YellowBackground.copy(alpha = 0.8f)
    )
    "Orange" -> ThemeColors(
        OrangeBackground, OrangeAccent, OrangeSnake,
        OrangeAccent.copy(alpha = 0.2f), OrangeBackground.copy(alpha = 0.8f)
    )
    "Black and White" -> ThemeColors(
        BWBackground, BWAccent, BWSnake,
        BWAccent.copy(alpha = 0.2f), BWBackground.copy(alpha = 0.8f)
    )
    else -> ThemeColors(
        DarkBackground, AccentBlue, SnakeColor,
        TopBarButtonBackground, BottomBarBackground
    )
}

@Composable
fun ArrowsTheme(
    themeName: String = "Dark",
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val themeColors = getThemeColors(themeName)
    val colorScheme = PuzzleColorScheme.copy(
        background = themeColors.background,
        surface = themeColors.topBarButton,
        surfaceVariant = themeColors.bottomBar,
        primary = themeColors.accent,
        secondary = themeColors.bottomBar,
        tertiary = themeColors.snake
    )
    CompositionLocalProvider(LocalThemeColors provides themeColors) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }
}
