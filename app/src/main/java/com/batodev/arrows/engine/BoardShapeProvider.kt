package com.batodev.arrows.engine

import android.graphics.Bitmap

interface BoardShapeProvider {
    fun getRandomShape(): Bitmap?
}
