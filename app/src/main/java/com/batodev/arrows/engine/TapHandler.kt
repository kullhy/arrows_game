package com.batodev.arrows.engine

import com.batodev.arrows.SoundManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val FLASH_DURATION_MS = 500L

class TapHandler(
    private val coroutineScope: CoroutineScope,
    private val soundManager: SoundManager?,
    private val onVibrate: () -> Unit
) {
    var flashingSnakeId: Int? = null
        private set

    fun handleSnakeTap(params: TapParams) {
        if (params.isVibrationEnabled) onVibrate()
        if (params.obstructed) {
            handleObstructed(params.snake, params.lives, params.onPenalty)
        } else {
            handleSuccessful(params.onSuccess)
        }
    }

    private fun handleObstructed(snake: Snake, lives: Int, onPenalty: () -> Unit) {
        if (lives > 0) {
            onPenalty()
            if (lives > 1) soundManager?.playLiveLost() else soundManager?.playGameLost()
        }
        flashingSnakeId = snake.id
        coroutineScope.launch {
            delay(FLASH_DURATION_MS)
            if (flashingSnakeId == snake.id) flashingSnakeId = null
        }
    }

    private fun handleSuccessful(onSuccess: () -> Unit) {
        soundManager?.playRandomSwitch()
        onSuccess()
    }
    
    fun clearFlash() { flashingSnakeId = null }
}
