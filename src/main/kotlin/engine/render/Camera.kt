package org.soyuz.engine.render

import org.soyuz.util.math.MathUtil

class Camera {
    private val projection = FloatArray(16)
    private var currentWidth = 0f
    private var currentHeight = 0f
    private var offsetX = 0f
    private var offsetY = 0f

    fun setOrtho(width: Float, height: Float) {
        currentWidth = width
        currentHeight = height
        rebuildProjection()
    }

    fun setOffset(x: Float, y: Float) {
        offsetX = x / (currentWidth / 2f)
        offsetY = y / (currentHeight / 2f)
        rebuildProjection()
    }

    private fun rebuildProjection() {
        MathUtil.orthoMatrix(currentWidth, currentHeight, offsetX, offsetY).copyInto(projection)
    }



    fun getProjection(): FloatArray = projection
}