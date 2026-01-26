package com.batodev.arrows.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.batodev.arrows.R
import com.batodev.arrows.engine.BoardShapeProvider
import kotlin.random.Random

class AndroidResourceBoardShapeProvider(private val context: Context) : BoardShapeProvider {
    
    private val shapeMap = mapOf(
        "bolt" to R.drawable.bolt_256dp_000000_fill1_wght400_grad0_opsz48,
        "brick" to R.drawable.brick_256dp_000000_fill1_wght400_grad0_opsz48,
        "build" to R.drawable.build_256dp_000000_fill1_wght400_grad0_opsz48,
        "cannabis" to R.drawable.cannabis_256dp_000000_fill1_wght400_grad0_opsz48,
        "chess_queen" to R.drawable.chess_queen_256dp_000000_fill1_wght400_grad0_opsz48,
        "chess_rook" to R.drawable.chess_rook_256dp_000000_fill1_wght400_grad0_opsz48,
        "disabled" to R.drawable.disabled_by_default_256dp_000000_fill1_wght400_grad0_opsz48,
        "favorite" to R.drawable.favorite_256dp_000000_fill1_wght400_grad0_opsz48,
        "heart" to R.drawable.heart,
        "home" to R.drawable.home_256dp_000000_fill1_wght400_grad0_opsz48,
        "humerus" to R.drawable.humerus_256dp_000000_fill1_wght400_grad0_opsz48,
        "key" to R.drawable.key_vertical_256dp_000000_fill1_wght400_grad0_opsz48,
        "star_kid" to R.drawable.kid_star_256dp_000000_fill1_wght400_grad0_opsz48,
        "lips" to R.drawable.lips_256dp_000000_fill1_wght400_grad0_opsz48,
        "mood_bad" to R.drawable.mood_bad_256dp_000000_fill1_wght400_grad0_opsz48,
        "person" to R.drawable.person_256dp_000000_fill1_wght400_grad0_opsz48,
        "satisfied" to R.drawable.sentiment_satisfied_256dp_000000_fill1_wght400_grad0_opsz48,
        "star" to R.drawable.star_256dp_000000_fill1_wght400_grad0_opsz48,
        "tibia" to R.drawable.tibia_256dp_000000_fill1_wght400_grad0_opsz48,
        "water_bottle" to R.drawable.water_bottle_large_256dp_000000_fill1_wght400_grad0_opsz48
    )

    override fun getRandomShape(): Bitmap? {
        val resId = shapeMap.values.random()
        return decodeResource(resId)
    }

    override fun getAllShapeNames(): List<String> = shapeMap.keys.toList().sorted()

    override fun getShapeByName(name: String): Bitmap? {
        val resId = shapeMap[name] ?: return null
        return decodeResource(resId)
    }

    private fun decodeResource(resId: Int): Bitmap? {
        return BitmapFactory.decodeResource(context.resources, resId, BitmapFactory.Options().apply {
            inScaled = false
        })
    }
}
