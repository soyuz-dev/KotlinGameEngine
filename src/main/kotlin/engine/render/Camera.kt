package org.soyuz.engine.render

class Camera {
    private val projection = FloatArray(16)

    fun setOrtho(width: Float, height: Float) {
        projection.fill(0f)
        projection[0] = 2f/width
        projection[5] = -2f/height
        projection[10] = -1f
        projection[12] = -1f
        projection[13] = 1f
        projection[15] = 1f
    }

    fun getProjection(): FloatArray = projection
}