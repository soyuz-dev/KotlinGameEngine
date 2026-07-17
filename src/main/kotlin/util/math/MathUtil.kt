package org.soyuz.util.math

import kotlin.math.cos
import kotlin.math.sin

object MathUtil {
    fun clamp(toClamp:Double, min: Double, max: Double) : Double {
        return if (toClamp > max) max else if (toClamp < min) min else toClamp
    }

    fun project(corners: List<Vector2D>, axis: Vector2D): Pair<Double, Double> {
        var min = Double.MAX_VALUE
        var max = -Double.MAX_VALUE
        for (corner in corners) {
            val projection = corner.dot(axis)
            if (projection < min) min = projection
            if (projection > max) max = projection
        }
        return   min to max
    }

// MathUtil.kt additions

    fun modelMatrix(transform: Transform): FloatArray {
        val m = FloatArray(16)
        val c = cos(transform.rotationRadians).toFloat()
        val s = sin(transform.rotationRadians).toFloat()
        val sx = transform.scale.x.toFloat()
        val sy = transform.scale.y.toFloat()
        val tx = transform.position.x.toFloat()
        val ty = transform.position.y.toFloat()

        // Column-major: scale * rotate * translate
        m[0] = c * sx;  m[4] = -s * sy; m[8]  = 0f; m[12] = tx
        m[1] = s * sx;  m[5] =  c * sy; m[9]  = 0f; m[13] = ty
        m[2] = 0f;      m[6] =  0f;     m[10] = 1f; m[14] = 0f
        m[3] = 0f;      m[7] =  0f;     m[11] = 0f; m[15] = 1f

        return m
    }

    fun orthoMatrix(width: Float, height: Float, offsetX: Float = 0f, offsetY: Float = 0f, rotation: Float = 0f): FloatArray {
        val c = cos(rotation)
        val s = sin(rotation)
        return floatArrayOf(
            2f / width * c, 2f / width * s, 0f, 0f,
            -2f / height * s, -2f / height * c, 0f, 0f,  // wait, need to think about this
            0f, 0f, -1f, 0f,
            -1f + offsetX, 1f + offsetY, 0f, 1f
        )
    }
}