package com.batodev.arrows.engine

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.get
import androidx.core.graphics.scale

private const val COLOR_THRESHOLD = 128

class BoardImageProcessor {

    fun createWallsFromImage(original: Bitmap, targetWidth: Int, targetHeight: Int): Array<BooleanArray> {
        val clipped = clipToBlackContent(original)
        val scaled = clipped.scale(targetWidth, targetHeight, false)
        val walls = Array(targetWidth) { BooleanArray(targetHeight) }

        for (x in 0 until targetWidth) {
            for (y in 0 until targetHeight) {
                val pixel = scaled[x, y]
                val isBlack = Color.alpha(pixel) > COLOR_THRESHOLD &&
                        Color.red(pixel) < COLOR_THRESHOLD &&
                        Color.green(pixel) < COLOR_THRESHOLD &&
                        Color.blue(pixel) < COLOR_THRESHOLD
                walls[x][y] = !isBlack
            }
        }
        return walls
    }

    private fun clipToBlackContent(bitmap: Bitmap): Bitmap {
        var minX = bitmap.width
        var minY = bitmap.height
        var maxX = 0
        var maxY = 0
        var found = false

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                if (isBlackPixel(bitmap[x, y])) {
                    minX = minOf(minX, x)
                    maxX = maxOf(maxX, x)
                    minY = minOf(minY, y)
                    maxY = maxOf(maxY, y)
                    found = true
                }
            }
        }

        if (!found) return bitmap

        val width = maxX - minX + 1
        val height = maxY - minY + 1
        return Bitmap.createBitmap(bitmap, minX, minY, width, height)
    }

    private fun isBlackPixel(pixel: Int): Boolean {
        return Color.alpha(pixel) > COLOR_THRESHOLD &&
                Color.red(pixel) < COLOR_THRESHOLD &&
                Color.green(pixel) < COLOR_THRESHOLD &&
                Color.blue(pixel) < COLOR_THRESHOLD
    }
}
