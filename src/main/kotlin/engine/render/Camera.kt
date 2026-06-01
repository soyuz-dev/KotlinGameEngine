package org.soyuz.engine.render

import org.soyuz.util.MathUtil

class Camera {
    private val projection = FloatArray(16)

    fun setOrtho(width: Float, height: Float) {
        MathUtil.orthoMatrix(width, height).copyInto(projection)
    }

    fun getProjection(): FloatArray = projection
}