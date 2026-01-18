package com.batodev.arrows.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

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

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun ArrowsTheme(
    themeName: String = "Dark",
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val themeColors = when (themeName) {
        "Green" -> ThemeColors(
            background = GreenBackground,
            accent = GreenAccent,
            snake = GreenSnake,
            topBarButton = GreenAccent.copy(alpha = 0.2f),
            bottomBar = GreenBackground.copy(alpha = 0.8f)
        )
        "Red" -> ThemeColors(
            background = RedBackground,
            accent = RedAccent,
            snake = RedSnake,
            topBarButton = RedAccent.copy(alpha = 0.2f),
            bottomBar = RedBackground.copy(alpha = 0.8f)
        )
        "Yellow" -> ThemeColors(
            background = YellowBackground,
            accent = YellowAccent,
            snake = YellowSnake,
            topBarButton = YellowAccent.copy(alpha = 0.2f),
            bottomBar = YellowBackground.copy(alpha = 0.8f)
        )
        "Orange" -> ThemeColors(
            background = OrangeBackground,
            accent = OrangeAccent,
            snake = OrangeSnake,
            topBarButton = OrangeAccent.copy(alpha = 0.2f),
            bottomBar = OrangeBackground.copy(alpha = 0.8f)
        )
        "Black and White" -> ThemeColors(
            background = BWBackground,
            accent = BWAccent,
            snake = BWSnake,
            topBarButton = BWAccent.copy(alpha = 0.2f),
            bottomBar = BWBackground.copy(alpha = 0.8f)
        )
        else -> ThemeColors(
            background = DarkBackground,
            accent = AccentBlue,
            snake = SnakeColor,
            topBarButton = TopBarButtonBackground,
            bottomBar = BottomBarBackground
        )
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && themeName == "Dark" -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme || themeName != "Light" -> DarkColorScheme.copy(
            background = themeColors.background,
            surface = themeColors.background,
            primary = themeColors.accent
        )
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalThemeColors provides themeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
