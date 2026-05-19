package org.soyuz.util

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
}