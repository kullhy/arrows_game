package com.batodev.arrows

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.batodev.arrows.ui.AppViewModel
import com.batodev.arrows.ui.theme.AccentBlue
import com.batodev.arrows.ui.theme.ArrowsTheme
import com.batodev.arrows.ui.theme.BottomBarBackground
import com.batodev.arrows.ui.theme.DarkBackground
import com.batodev.arrows.ui.theme.InactiveIcon
import com.batodev.arrows.ui.theme.NavigationIndicator
import com.batodev.arrows.ui.theme.TopBarButtonBackground
import com.batodev.arrows.ui.theme.White
import com.google.android.play.core.review.ReviewManagerFactory

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val application = applicationContext as ArrowsApplication
        setContent {
            val viewModel: AppViewModel = viewModel(
                factory = AppViewModel.Factory(application.userPreferencesRepository)
            )
            val currentTheme by viewModel.theme.collectAsState()

            ArrowsTheme(darkTheme = currentTheme == "Dark") {
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: AppViewModel) {
    val context = LocalContext.current

    var showThemeDialog by remember { mutableStateOf(false) }
    val currentTheme by viewModel.theme.collectAsState()

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = {
                viewModel.saveTheme(it)
                showThemeDialog = false
            }
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = BottomBarBackground,
                contentColor = White
            ) {
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Group 1: Preferences
            SettingsGroup {
                SettingsSwitchItem(
                    icon = Icons.Default.Vibration,
                    title = "Vibrations",
                    initialValue = true
                )
                SettingsSwitchItem(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    title = "Sounds",
                    initialValue = true
                )
                SettingsClickableItem(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    valueText = currentTheme,
                    onClick = { showThemeDialog = true }
                )
            }

            // Group 2: Feedback
            SettingsGroup {
                SettingsClickableItem(
                    icon = Icons.Default.Star,
                    title = "Rate us",
                    onClick = { launchReviewFlow(context) }
                )
                SettingsClickableItem(
                    icon = Icons.Default.Edit,
                    title = "Write us",
                    onClick = { launchEmail(context) }
                )
                SettingsClickableItem(
                    icon = Icons.Default.Apps,
                    title = "More Games",
                    onClick = { launchBrowser(context, "https://play.google.com/store/apps/dev?id=8228670503574649511") }
                )
            }

            // Group 3: Purchases
            SettingsGroup {
                SettingsSwitchItem(
                    icon = Icons.Default.Block,
                    title = "Remove Ads",
                    initialValue = false
                )
            }

            // Group 4: Legal
            SettingsGroup {
                SettingsClickableItem(
                    icon = Icons.Default.Description,
                    title = "Privacy",
                    onClick = { launchBrowser(context, "https://robmat.github.io/privacy_policy.html") }
                )
            }
        }
    }
}

fun launchBrowser(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
    }
}

fun launchEmail(context: Context) {
    try {
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
        val version = packageInfo.versionName
        val device = "${Build.MANUFACTURER} ${Build.MODEL} (SDK ${Build.VERSION.SDK_INT})"

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@emberfox.online"))
            putExtra(Intent.EXTRA_SUBJECT, "Arrows Game Support")
            putExtra(Intent.EXTRA_TEXT, "\n\n\n---\nApp Version: $version\nDevice: $device")
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Could not open email app", Toast.LENGTH_SHORT).show()
    }
}

fun launchReviewFlow(context: Context) {
    if (context !is Activity) return

    val manager = ReviewManagerFactory.create(context)
    val request = manager.requestReviewFlow()
    request.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val reviewInfo = task.result
            val flow = manager.launchReviewFlow(context, reviewInfo)
            flow.addOnCompleteListener { _ ->
                // The flow has finished. The API does not indicate whether the user
                // reviewed or not, or even whether the review dialog was shown.
                // Thus, no matter the result, we continue our app flow.
            }
        } else {
            // There was some problem, continue regardless of the result.
            // You can log the error here.
            Toast.makeText(context, "Could not launch review flow", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun SettingsGroup(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TopBarButtonBackground)
            .padding(vertical = 8.dp)
    ) {
        content()
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    initialValue: Boolean,
    onCheckedChange: ((Boolean) -> Unit)? = null
) {
    var checked by remember { mutableStateOf(initialValue) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = White.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
                onCheckedChange?.invoke(it)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = White,
                checkedTrackColor = AccentBlue,
                uncheckedThumbColor = White,
                uncheckedTrackColor = InactiveIcon
            )
        )
    }
}

@Composable
fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    valueText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = White.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (valueText != null) {
            Text(
                text = valueText,
                color = White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = "Open",
            tint = White.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: String,
    onDismiss: () -> Unit,
    onThemeSelected: (String) -> Unit
) {
    val themes = listOf("Dark") // Ready to add more: "Light", "System", "Blue", etc.

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BottomBarBackground,
        title = {
            Text(
                text = "Choose Theme",
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
                            selected = theme == currentTheme,
                            onClick = { onThemeSelected(theme) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AccentBlue,
                                unselectedColor = InactiveIcon
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = theme,
                            color = White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AccentBlue)
            }
        }
    )
}
