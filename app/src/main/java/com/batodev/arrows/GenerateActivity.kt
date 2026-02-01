package com.batodev.arrows

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.batodev.arrows.data.AndroidResourceBoardShapeProvider
import com.batodev.arrows.ui.AppViewModel
import com.batodev.arrows.ui.theme.ArrowsTheme
import com.batodev.arrows.ui.theme.LocalThemeColors
import com.batodev.arrows.ui.theme.ThemeColors
import com.batodev.arrows.ui.theme.White

private const val MIN_SIZE = 20f
private const val MAX_SIZE = 100f
private const val DEFAULT_SIZE = 50f
private const val RECTANGULAR_SHAPE = "rectangular"
private const val SHAPE_ICON_SIZE = 32

class GenerateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val application = applicationContext as ArrowsApplication
        setContent {
            val viewModel: AppViewModel = viewModel(
                factory = AppViewModel.Factory(application.userPreferencesRepository)
            )
            val currentTheme by viewModel.theme.collectAsState()

            ArrowsTheme(themeName = currentTheme) {
                GenerateScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateScreen() {
    val context = LocalContext.current
    val application = context.applicationContext as ArrowsApplication
    val viewModel: AppViewModel = viewModel(
        factory = AppViewModel.Factory(application.userPreferencesRepository)
    )
    val hasSavedLevel by viewModel.hasSavedLevel.collectAsState()
    val themeColors = LocalThemeColors.current

    var width by remember { mutableFloatStateOf(DEFAULT_SIZE) }
    var height by remember { mutableFloatStateOf(DEFAULT_SIZE) }
    var selectedShape by remember { mutableStateOf(RECTANGULAR_SHAPE) }
    var showWarning by remember { mutableStateOf(false) }

    val shapeProvider = remember { AndroidResourceBoardShapeProvider(context) }
    val shapes = remember { listOf(RECTANGULAR_SHAPE) + shapeProvider.getAllShapeNames() }

    fun startCustomGame() {
        viewModel.regenerateCurrentLevel()
        val intent = Intent(context, GameActivity::class.java).apply {
            putExtra("IS_CUSTOM", true)
            putExtra("CUSTOM_WIDTH", width.toInt())
            putExtra("CUSTOM_HEIGHT", height.toInt())
            val shapeName = if (selectedShape == RECTANGULAR_SHAPE) null else selectedShape
            putExtra("CUSTOM_SHAPE", shapeName)
        }
        context.startActivity(intent)
    }

    if (showWarning) {
        WarningDialog(
            themeColors = themeColors,
            onConfirm = { startCustomGame() },
            onDismiss = { showWarning = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.custom_gen_title), color = White) },
                navigationIcon = {
                    IconButton(
                        onClick = { (context as? Activity)?.finish() },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = White)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_description_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = themeColors.background)
            )
        },
        containerColor = themeColors.background
    ) { innerPadding ->
        val state = GenerateContentState(
            innerPadding = innerPadding, width = width, height = height, shapes = shapes,
            selectedShape = selectedShape, themeColors = themeColors,
            onWidthChange = { width = it }, onHeightChange = { height = it },
            onShapeSelected = { selectedShape = it },
            onStartClick = { if (hasSavedLevel) showWarning = true else startCustomGame() }
        )
        GenerateContent(state)
    }
}

@Composable
private fun WarningDialog(themeColors: ThemeColors, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_gen_warning_title), color = White) },
        text = { Text(stringResource(R.string.custom_gen_warning_message), color = White) },
        confirmButton = {
            Button(onClick = { onConfirm(); onDismiss() }) {
                Text(stringResource(R.string.proceed_label))
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_label), color = White)
            }
        },
        containerColor = themeColors.background
    )
}

private data class GenerateContentState(
    val innerPadding: PaddingValues,
    val width: Float,
    val height: Float,
    val shapes: List<String>,
    val selectedShape: String,
    val themeColors: ThemeColors,
    val onWidthChange: (Float) -> Unit,
    val onHeightChange: (Float) -> Unit,
    val onShapeSelected: (String) -> Unit,
    val onStartClick: () -> Unit
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GenerateContent(state: GenerateContentState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(state.innerPadding)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SizeSlider(
            label = stringResource(R.string.width_label),
            value = state.width,
            onValueChange = state.onWidthChange,
            themeColors = state.themeColors
        )

        Spacer(modifier = Modifier.height(24.dp))

        SizeSlider(
            label = stringResource(R.string.height_label),
            value = state.height,
            onValueChange = state.onHeightChange,
            themeColors = state.themeColors
        )

        Spacer(modifier = Modifier.height(32.dp))

        ShapeSelectionSection(state.shapes, state.selectedShape, state.themeColors, state.onShapeSelected)

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = state.onStartClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = state.themeColors.accent),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier. width(12.dp))
            Text(
                text = stringResource(R.string.generate_start_label),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShapeSelectionSection(
    shapes: List<String>,
    selectedShape: String,
    themeColors: ThemeColors,
    onShapeSelected: (String) -> Unit
) {
    Text(
        text = stringResource(R.string.shape_label),
        color = White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        shapes.forEach { shape ->
            ShapeItem(
                name = shape,
                isSelected = selectedShape == shape,
                onClick = { onShapeSelected(shape) },
                themeColors = themeColors
            )
        }
    }
}

@Composable
private fun SizeSlider(label: String, value: Float, onValueChange: (Float) -> Unit, themeColors: ThemeColors) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, color = White, fontSize = 16.sp)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = value.toInt().toString(),
                color = themeColors.accent,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = MIN_SIZE..MAX_SIZE,
            colors = SliderDefaults.colors(
                thumbColor = themeColors.accent,
                activeTrackColor = themeColors.accent,
                inactiveTrackColor = themeColors.topBarButton
            )
        )
    }
}

@Composable
private fun ShapeItem(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    themeColors: ThemeColors
) {
    Card(
        modifier = Modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) themeColors.accent else themeColors.topBarButton,
            contentColor = White
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier.padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            ShapeIcon(name)
        }
    }
}

@Composable
private fun ShapeIcon(name: String) {
    if (name == RECTANGULAR_SHAPE) {
        Icon(
            imageVector = Icons.Default.CropSquare,
            contentDescription = stringResource(R.string.shape_rectangular),
            modifier = Modifier.size(SHAPE_ICON_SIZE.dp)
        )
    } else {
        val resId = getShapeResourceId(name)
        if (resId != null) {
            Icon(
                painter = painterResource(id = resId),
                contentDescription = name,
                modifier = Modifier.size(SHAPE_ICON_SIZE.dp)
            )
        } else {
            Text(text = name.take(1).uppercase())
        }
    }
}

private val shapeResourceMap = mapOf(
    "bolt" to R.drawable.bolt_256dp_000000_fill1_wght400_grad0_opsz48,
    "brick" to R.drawable.brick_256dp_000000_fill1_wght400_grad0_opsz48,
    "build" to R.drawable.build_256dp_000000_fill1_wght400_grad0_opsz48,
    "cannabis" to R.drawable.cannabis_256dp_000000_fill1_wght400_grad0_opsz48,
    "chess_queen" to R.drawable.chess_queen_256dp_000000_fill1_wght400_grad0_opsz48,
    "chess_rook" to R.drawable.chess_rook_256dp_000000_fill1_wght400_grad0_opsz48,
    "disabled" to R.drawable.disabled_by_default_256dp_000000_fill1_wght400_grad0_opsz48,
    "favorite" to R.drawable.favorite_256dp_000000_fill1_wght400_grad0_opsz48,
    "home" to R.drawable.home_256dp_000000_fill1_wght400_grad0_opsz48,
    "humerus" to R.drawable.humerus_256dp_000000_fill1_wght400_grad0_opsz48,
    "key" to R.drawable.key_vertical_256dp_000000_fill1_wght400_grad0_opsz48,
    "star_kid" to R.drawable.kid_star_256dp_000000_fill1_wght400_grad0_opsz48,
    "mood_bad" to R.drawable.mood_bad_256dp_000000_fill1_wght400_grad0_opsz48,
    "triangle" to R.drawable.change_history_256dp_000000_fill1_wght400_grad0_opsz48,
    "satisfied" to R.drawable.sentiment_satisfied_256dp_000000_fill1_wght400_grad0_opsz48,
    "star" to R.drawable.star_256dp_000000_fill1_wght400_grad0_opsz48,
    "tibia" to R.drawable.tibia_256dp_000000_fill1_wght400_grad0_opsz48,
    "water_bottle" to R.drawable.water_bottle_large_256dp_000000_fill1_wght400_grad0_opsz48
)

private fun getShapeResourceId(name: String): Int? {
    return shapeResourceMap[name]
}
