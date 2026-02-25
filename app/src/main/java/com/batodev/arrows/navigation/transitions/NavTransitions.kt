package com.batodev.arrows.navigation.transitions

import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import com.bumble.appyx.core.navigation.transition.TransitionParams
import com.bumble.appyx.navmodel.backstack.BackStack
import kotlin.math.roundToInt

private const val TRANSITION_DURATION_MS = 350

internal fun Modifier.applyNavTransition(
    transition: Transition<BackStack.State>,
    type: NavTransitionType,
    params: TransitionParams,
): Modifier = when (type) {
    NavTransitionType.FADE -> composedFade(transition)
    NavTransitionType.SLIDE_HORIZONTAL -> composedSlideHorizontal(transition, params)
    NavTransitionType.SLIDE_VERTICAL -> composedSlideVertical(transition, params)
    NavTransitionType.SCALE_FADE -> composedScaleFade(transition)
    NavTransitionType.ROTATE_FADE -> composedRotateFade(transition)
}

private fun Modifier.composedFade(
    transition: Transition<BackStack.State>,
): Modifier = composed {
    val alpha by transition.animateFloat(
        transitionSpec = { tween(TRANSITION_DURATION_MS) },
        label = "fade-alpha",
        targetValueByState = { state ->
            when (state) {
                BackStack.State.ACTIVE -> 1f
                else -> 0f
            }
        }
    )
    graphicsLayer { this.alpha = alpha }
}

private fun Modifier.composedSlideHorizontal(
    transition: Transition<BackStack.State>,
    params: TransitionParams,
): Modifier = composed {
    val xFraction by transition.animateFloat(
        transitionSpec = { tween(TRANSITION_DURATION_MS) },
        label = "slide-h-x",
        targetValueByState = { state ->
            when (state) {
                BackStack.State.CREATED -> 1f
                BackStack.State.ACTIVE -> 0f
                BackStack.State.STASHED -> -0.25f
                BackStack.State.DESTROYED -> 1f
            }
        }
    )
    val alpha by transition.animateFloat(
        transitionSpec = { tween(TRANSITION_DURATION_MS) },
        label = "slide-h-alpha",
        targetValueByState = { state ->
            when (state) {
                BackStack.State.ACTIVE -> 1f
                else -> 0f
            }
        }
    )
    val widthDp = params.bounds.width.value
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
            placeable.placeRelative(
                x = (widthDp * xFraction * density).roundToInt(),
                y = 0,
            )
        }
    }.graphicsLayer { this.alpha = alpha }
}

private fun Modifier.composedSlideVertical(
    transition: Transition<BackStack.State>,
    params: TransitionParams,
): Modifier = composed {
    val yFraction by transition.animateFloat(
        transitionSpec = { tween(TRANSITION_DURATION_MS) },
        label = "slide-v-y",
        targetValueByState = { state ->
            when (state) {
                BackStack.State.CREATED -> 1f
                BackStack.State.ACTIVE -> 0f
                BackStack.State.STASHED -> -0.25f
                BackStack.State.DESTROYED -> 1f
            }
        }
    )
    val alpha by transition.animateFloat(
        transitionSpec = { tween(TRANSITION_DURATION_MS) },
        label = "slide-v-alpha",
        targetValueByState = { state ->
            when (state) {
                BackStack.State.ACTIVE -> 1f
                else -> 0f
            }
        }
    )
    val heightDp = params.bounds.height.value
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
            placeable.placeRelative(
                x = 0,
                y = (heightDp * yFraction * density).roundToInt(),
            )
        }
    }.graphicsLayer { this.alpha = alpha }
}

private fun Modifier.composedScaleFade(
    transition: Transition<BackStack.State>,
): Modifier = composed {
    val scale by transition.animateFloat(
        transitionSpec = { tween(TRANSITION_DURATION_MS) },
        label = "scale-scale",
        targetValueByState = { state ->
            when (state) {
                BackStack.State.ACTIVE -> 1f
                else -> 0.85f
            }
        }
    )
    val alpha by transition.animateFloat(
        transitionSpec = { tween(TRANSITION_DURATION_MS) },
        label = "scale-alpha",
        targetValueByState = { state ->
            when (state) {
                BackStack.State.ACTIVE -> 1f
                else -> 0f
            }
        }
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    }
}

private fun Modifier.composedRotateFade(
    transition: Transition<BackStack.State>,
): Modifier = composed {
    val rotation by transition.animateFloat(
        transitionSpec = { tween(TRANSITION_DURATION_MS) },
        label = "rotate-rotation",
        targetValueByState = { state ->
            when (state) {
                BackStack.State.CREATED -> 8f
                BackStack.State.ACTIVE -> 0f
                BackStack.State.STASHED -> -8f
                BackStack.State.DESTROYED -> 8f
            }
        }
    )
    val alpha by transition.animateFloat(
        transitionSpec = { tween(TRANSITION_DURATION_MS) },
        label = "rotate-alpha",
        targetValueByState = { state ->
            when (state) {
                BackStack.State.ACTIVE -> 1f
                else -> 0f
            }
        }
    )
    graphicsLayer {
        rotationZ = rotation
        this.alpha = alpha
    }
}
