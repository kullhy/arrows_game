package com.batodev.arrows.ui.game

import com.batodev.arrows.GameConstants
import kotlin.random.Random

data class CelebrationContent(val labelResId: Int, val videoResId: Int)

object CelebrationContentSelector {

    fun selectContent(random: Random = Random): CelebrationContent {
        val label = GameConstants.CONGRATULATION_LABELS.random(random)
        val video = GameConstants.WIN_VIDEOS.random(random)
        return CelebrationContent(labelResId = label, videoResId = video)
    }
}
