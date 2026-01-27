package com.batodev.arrows.engine

private const val COLOR_THRESHOLD = 128
private const val ALPHA_SHIFT = 24
private const val RED_SHIFT = 16
private const val GREEN_SHIFT = 8
private const val COLOR_MASK = 0xFF

class BoardImageProcessor {

    fun createWallsFromImage(original: NativeImage, targetWidth: Int, targetHeight: Int): Array<BooleanArray> {
        val clipped = clipToBlackContent(original)
        val scaled = clipped.scale(targetWidth, targetHeight)
        val walls = Array(targetWidth) { BooleanArray(targetHeight) }

        for (x in 0 until targetWidth) {
            for (y in 0 until targetHeight) {
                val pixel = scaled.getPixel(x, y)
                walls[x][y] = !isBlackPixel(pixel)
            }
        }
        return walls
    }

    private fun clipToBlackContent(image: NativeImage): NativeImage {
        var minX = image.width
        var minY = image.height
        var maxX = 0
        var maxY = 0
        var found = false

        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                if (isBlackPixel(image.getPixel(x, y))) {
                    minX = minOf(minX, x)
                    maxX = maxOf(maxX, x)
                    minY = minOf(minY, y)
                    maxY = maxOf(maxY, y)
                    found = true
                }
            }
        }

        if (!found) return image

        return image.clip(minX, minY, maxX, maxY)
    }

    private fun isBlackPixel(pixel: Int): Boolean {
        val alpha = (pixel shr ALPHA_SHIFT) and COLOR_MASK
        val red = (pixel shr RED_SHIFT) and COLOR_MASK
        val green = (pixel shr GREEN_SHIFT) and COLOR_MASK
        val blue = pixel and COLOR_MASK
        
        return alpha > COLOR_THRESHOLD &&
                red < COLOR_THRESHOLD &&
                green < COLOR_THRESHOLD &&
                blue < COLOR_THRESHOLD
    }
}
