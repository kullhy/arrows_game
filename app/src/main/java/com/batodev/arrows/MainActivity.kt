package com.batodev.arrows

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.batodev.arrows.ui.AppNavigationBar
import com.batodev.arrows.ui.AppViewModel
import com.batodev.arrows.ui.NavigationDestination
import com.batodev.arrows.ui.ads.BannerAdView
import com.batodev.arrows.ui.theme.ArrowsTheme
import com.batodev.arrows.ui.theme.LocalThemeColors
import com.batodev.arrows.ui.theme.ThemeColors
import com.batodev.arrows.ui.theme.White

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val application = applicationContext as ArrowsApplication
        setContent {
            val viewModel: AppViewModel = viewModel(
                factory = AppViewModel.Factory(application.userPreferencesRepository, application.gameStateDao)
            )
            val currentTheme by viewModel.theme.collectAsState()

            ArrowsTheme(themeName = currentTheme) {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val application = context.applicationContext as ArrowsApplication
    val viewModel: AppViewModel = viewModel(
        factory = AppViewModel.Factory(application.userPreferencesRepository, application.gameStateDao)
    )
    val hasSavedLevel by viewModel.hasSavedLevel.collectAsState()
    val levelNumber by viewModel.levelNumber.collectAsState()
    val isAdFree by viewModel.isAdFree.collectAsState()
    val themeColors = LocalThemeColors.current

    Scaffold(
        containerColor = themeColors.background,
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!isAdFree) {
                    BannerAdView()
                }
                AppNavigationBar(
                    selectedDestination = NavigationDestination.HOME,
                    levelNumber = levelNumber,
                    themeColors = themeColors
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))
            LogoSection(levelNumber, themeColors)
            Spacer(modifier = Modifier.weight(1f))
            PlayButton(hasSavedLevel, themeColors)
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}


@Composable
private fun LogoSection(levelNumber: Int, themeColors: ThemeColors) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TriangleIcon(modifier = Modifier.size(40.dp), color = White)
            Text(
                text = stringResource(R.string.logo_text),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.level_label, levelNumber), fontSize = 24.sp,
            fontWeight = FontWeight.Bold, color = themeColors.accent
        )
    }
}

@Composable
private fun PlayButton(isContinue: Boolean, themeColors: ThemeColors) {
    val context = LocalContext.current
    Button(
        onClick = {
            val intent = Intent(context, GameActivity::class.java)
            context.startActivity(intent)
        },
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
        shape = RoundedCornerShape(28.dp)
    ) {
        val text = if (isContinue) {
            stringResource(R.string.continue_label)
        } else {
            stringResource(R.string.play_label)
        }
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TriangleIcon(modifier: Modifier = Modifier, color: Color) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width / 2f, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path = path, color = color)
    }
}
