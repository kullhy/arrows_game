package com.batodev.arrows

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
import com.batodev.arrows.data.AndroidResourceBoardShapeProvider
import com.batodev.arrows.data.ShapeRegistry
import com.batodev.arrows.navigation.NavTarget
import com.batodev.arrows.ui.AppNavigationBar
import com.batodev.arrows.ui.AppViewModel
import com.batodev.arrows.ui.NavigationDestination
import com.batodev.arrows.ui.ads.BannerAdView
import com.batodev.arrows.ui.theme.LocalThemeColors
import com.batodev.arrows.ui.theme.ThemeColors
import com.batodev.arrows.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateScreen(
    appViewModel: AppViewModel,
    onStartCustomGame: (NavTarget.Game) -> Unit,
    onBack: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val hasSavedLevel by appViewModel.hasSavedLevel.collectAsState()
    val isFillBoardEnabled by appViewModel.isFillBoardEnabled.collectAsState()
    val levelNumber by appViewModel.levelNumber.collectAsState()
    val isAdFree by appViewModel.isAdFree.collectAsState()
    val themeColors = LocalThemeColors.current
    val maxSize = if (isFillBoardEnabled) GameConstants.GENERATOR_MAX_SIZE_FILL_BOARD
                  else GameConstants.GENERATOR_MAX_SIZE
    var width by remember { mutableFloatStateOf(GameConstants.GENERATOR_DEFAULT_SIZE) }
    var height by remember { mutableFloatStateOf(GameConstants.GENERATOR_DEFAULT_SIZE) }
    var selectedShape by remember { mutableStateOf(GameConstants.SHAPE_TYPE_RECTANGULAR) }
    var showWarning by remember { mutableStateOf(false) }
    if (width > maxSize) width = maxSize
    if (height > maxSize) height = maxSize
    val shapeProvider = remember { AndroidResourceBoardShapeProvider(context) }
    val shapes = remember {
        listOf(GameConstants.SHAPE_TYPE_RECTANGULAR) + shapeProvider.getAllShapeNames()
    }
    if (showWarning) {
        WarningDialog(
            themeColors = themeColors,
            onConfirm = { startCustomGameNavigation(appViewModel, onStartCustomGame, width, selectedShape, height) },
            onDismiss = { showWarning = false }
        )
    }
    val scaffoldState = GenerateScaffoldState(
        context, themeColors, levelNumber, width, height, maxSize, shapes, selectedShape, isAdFree,
        { width = it }, { height = it }, { selectedShape = it }, onBack, onNavigateHome, onNavigateToSettings
    ) {
        if (hasSavedLevel) showWarning = true
        else startCustomGameNavigation(appViewModel, onStartCustomGame, width, selectedShape, height)
    }
    GenerateScaffoldContent(scaffoldState)
}

private data class GenerateScaffoldState(
    val context: android.content.Context,
    val themeColors: ThemeColors,
    val levelNumber: Int,
    val width: Float,
    val height: Float,
    val maxSize: Float,
    val shapes: List<String>,
    val selectedShape: String,
    val isAdFree: Boolean,
    val onWidthChange: (Float) -> Unit,
    val onHeightChange: (Float) -> Unit,
    val onShapeSelected: (String) -> Unit,
    val onBack: () -> Unit,
    val onNavigateHome: () -> Unit,
    val onNavigateToSettings: () -> Unit,
    val onStartClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenerateScaffoldContent(state: GenerateScaffoldState) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.custom_gen_title), color = White) },
                navigationIcon = {
                    IconButton(
                        onClick = state.onBack,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = White)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_description_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = state.themeColors.background)
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!state.isAdFree) {
                    BannerAdView()
                }
                AppNavigationBar(
                    selectedDestination = NavigationDestination.GENERATOR,
                    levelNumber = state.levelNumber,
                    themeColors = state.themeColors,
                    onNavigateHome = state.onNavigateHome,
                    onNavigateToGenerate = {},
                    onNavigateToSettings = state.onNavigateToSettings
                )
            }
        },
        containerColor = state.themeColors.background
    ) { innerPadding ->
        val contentState = GenerateContentState(
            innerPadding = innerPadding, width = state.width, height = state.height,
            maxSize = state.maxSize, shapes = state.shapes, selectedShape = state.selectedShape,
            themeColors = state.themeColors,
            onWidthChange = state.onWidthChange, onHeightChange = state.onHeightChange,
            onShapeSelected = state.onShapeSelected,
            onStartClick = state.onStartClick
        )
        GenerateContent(contentState)
    }
}

private fun startCustomGameNavigation(
    viewModel: AppViewModel,
    onStartCustomGame: (NavTarget.Game) -> Unit,
    width: Float,
    selectedShape: String,
    height: Float
) {
    viewModel.regenerateCurrentLevel()
    val shapeName = if (selectedShape == GameConstants.SHAPE_TYPE_RECTANGULAR) null else selectedShape
    val gameTarget = NavTarget.Game(
        isCustom = true,
        customWidth = width.toInt(),
        customHeight = height.toInt(),
        customShape = shapeName
    )
    onStartCustomGame(gameTarget)
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
    val maxSize: Float,
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
            maxSize = state.maxSize,
            onValueChange = state.onWidthChange,
            themeColors = state.themeColors
        )

        Spacer(modifier = Modifier.height(24.dp))

        SizeSlider(
            label = stringResource(R.string.height_label),
            value = state.height,
            maxSize = state.maxSize,
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
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
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
private fun SizeSlider(
    label: String,
    value: Float,
    maxSize: Float,
    onValueChange: (Float) -> Unit,
    themeColors: ThemeColors
) {
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
            valueRange = GameConstants.GENERATOR_MIN_SIZE..maxSize,
            modifier = Modifier.padding(horizontal = 12.dp),
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
    if (name == GameConstants.SHAPE_TYPE_RECTANGULAR) {
        Icon(
            imageVector = Icons.Default.CropSquare,
            contentDescription = stringResource(R.string.shape_rectangular),
            modifier = Modifier.size(GameConstants.SHAPE_ICON_SIZE.dp)
        )
    } else {
        val resId = getShapeResourceId(name)
        if (resId != null) {
            Icon(
                painter = painterResource(id = resId),
                contentDescription = name,
                modifier = Modifier.size(GameConstants.SHAPE_ICON_SIZE.dp)
            )
        } else {
            Text(text = name.take(1).uppercase())
        }
    }
}


private fun getShapeResourceId(name: String): Int? {
    return ShapeRegistry.shapes[name]
}
