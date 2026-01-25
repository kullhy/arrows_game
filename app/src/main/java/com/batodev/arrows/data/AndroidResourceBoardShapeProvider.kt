package com.batodev.arrows.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.batodev.arrows.R
import com.batodev.arrows.engine.BoardShapeProvider
import kotlin.random.Random

class AndroidResourceBoardShapeProvider(private val context: Context) : BoardShapeProvider {
    
    private val shapeResIds = listOf(
        R.drawable.bolt_256dp_000000_fill1_wght400_grad0_opsz48,
        R.drawable.brick_256dp_000000_fill1_wght400_grad0_opsz48,
        R.drawable.build_256dp_000000_fill1_wght400_grad0_opsz48,
        R.drawable.cannabis_256dp_000000_fill1_wght400_grad0_opsz48,
        R.drawable.chess_queen_256dp_000000_fill1_wght400_grad0_opsz48,
        R.drawable.chess_rook_256dp_000000_fill1_wght400_grad0_opsz48,
        R.drawable.disabled_by_default_256dp_000000_fill1_wght400_grad0_opsz48,
        R.drawable.favorite_256dp_000000_fill1_wght400_grad0_opsz48,
        R.drawable.heart,
        R.drawable.home_256dp_000000_fill1_wght400_grad0_opsz48,
        R.drawable.humerus_256dp_000000_fill1_wght400_grad0_opsz48,
        R.drawable.key_vertical_256dp_000000_fill1_wght400_grad0_opsz48,
        R.drawable.kid_star_256dp_000000_fill1_wght400_grad0_opsz48,
        R.drawable.lips_256dp_000000_fill1_wght400_grad0_opsz48,
        R.drawable.mood_bad_256dp_000000_fill1_wght400_grad0_opsz48,
        R.drawable.person_256dp_000000_fill1_wght400_grad0_opsz48,
        R.drawable.sentiment_satisfied_256dp_000000_fill1_wght400_grad0_opsz48,
        R.drawable.star_256dp_000000_fill1_wght400_grad0_opsz48,
        R.drawable.tibia_256dp_000000_fill1_wght400_grad0_opsz48,
        R.drawable.water_bottle_large_256dp_000000_fill1_wght400_grad0_opsz48
    )

    override fun getRandomShape(): Bitmap? {
        val resId = shapeResIds.random()
        return BitmapFactory.decodeResource(context.resources, resId, BitmapFactory.Options().apply {
            inScaled = false
        })
    }
}
