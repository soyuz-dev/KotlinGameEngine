package org.soyuz.engine.render

import org.soyuz.util.math.Matrix4f

class Camera {

    private var width = 0f
    private var height = 0f

    private var positionX = 0f
    private var positionY = 0f

    private var zoom = 1f

    private var projection = Matrix4f()
    private var viewProjection = Matrix4f()

    fun setOrtho(width: Float, height: Float) {
        this.width = width
        this.height = height
        rebuild()
    }

    fun setPosition(x: Float, y: Float) {
        positionX = x
        positionY = y
        rebuild()
    }

    fun setZoom(scale: Float) {
        zoom = scale
        rebuild()
    }

    fun zoom(factor: Float) {
        zoom *= factor
        rebuild()
    }

    private fun rebuild() {
        projection = Matrix4f.ortho(
            width / zoom,
            height / zoom
        )

        val view =
            Matrix4f.identity()
                .translate(
                    positionX - width / 2f,
                    positionY - height / 2f
                )

        viewProjection = projection * view
    }

    fun getProjection(): FloatArray {
        return viewProjection.toFloatArray()
    }
}