package com.batodev.arrows.engine

import android.graphics.Bitmap

interface BoardShapeProvider {
    fun getRandomShape(): Bitmap?
    fun getAllShapeNames(): List<String>
    fun getShapeByName(name: String): Bitmap?
}
