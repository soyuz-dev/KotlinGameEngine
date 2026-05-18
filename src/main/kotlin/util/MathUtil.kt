package org.soyuz.util

object MathUtil {
    fun clamp(toClamp:Double, min: Double, max: Double) : Double {
        return if (toClamp > max) max else if (toClamp < min) min else toClamp
    }
}