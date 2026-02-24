package com.batodev.arrows.navigation.transitions

import kotlin.random.Random

class TransitionPicker(
    val types: List<NavTransitionType> = NavTransitionType.entries,
    private val random: Random = Random.Default,
) {
    fun pick(): NavTransitionType = types.random(random)
}
