package com.batodev.arrows

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.batodev.arrows.engine.GameEngine
import com.batodev.arrows.ui.theme.ArrowsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArrowsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(Color.White), // Clean white background like the image
                        contentAlignment = Alignment.Center
                    ) {
                        ArrowsGameView()
                    }
                }
            }
        }
    }
}

@Composable
fun ArrowsGameView() {
    val engine = remember { GameEngine() }
    var level by remember { mutableStateOf(engine.generateSolvableLevel(7, 17, 1.0)) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Board container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(7f / 11f)
                .padding(16.dp)
        ) {
            ArrowsBoardRenderer.Board(level = level, modifier = Modifier.fillMaxSize())
        }
        Spacer(modifier = Modifier.height(16.dp))
        androidx.compose.material3.Button(onClick = {
            level = engine.generateSolvableLevel(7, 17, 1.0)
        }) {
            androidx.compose.material3.Text("Regenerate Board")
        }
    }
}
