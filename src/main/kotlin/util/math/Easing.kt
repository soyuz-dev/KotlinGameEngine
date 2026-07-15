package org.soyuz.util.math

import kotlin.math.*

typealias EasingFunction = (Double) -> Double

object Easing {

    // --- Polynomial-based (return Polynomial, can be called as functions) ---

    val linear: EasingFunction = Polynomial(0.0, 1.0)
    val quadIn: EasingFunction = Polynomial(0.0, 0.0, 1.0)
    val quadOut: EasingFunction = Polynomial(0.0, 2.0, -1.0)
    val cubicIn: EasingFunction = Polynomial(0.0, 0.0, 0.0, 1.0)
    val cubicOut: EasingFunction = Polynomial(0.0, 3.0, -3.0, 1.0)
    val quartIn: EasingFunction = Polynomial(0.0, 0.0, 0.0, 0.0, 1.0)
    val quartOut: EasingFunction = Polynomial(0.0, 4.0, -6.0, 4.0, -1.0)
    val quintIn: EasingFunction = Polynomial(0.0, 0.0, 0.0, 0.0, 0.0, 1.0)
    val quintOut: EasingFunction = Polynomial(0.0, 5.0, -10.0, 10.0, -5.0, 1.0)

    // --- Piecewise polynomial (must be functions) ---

    val quadInOut: EasingFunction = { t ->
        if (t < 0.5) 2.0 * t * t
        else 1.0 - (-2.0 * t + 2.0).let { it * it } / 2.0
    }

    val cubicInOut: EasingFunction = { t ->
        if (t < 0.5) 4.0 * t * t * t
        else 1.0 - (-2.0 * t + 2.0).let { it * it * it } / 2.0
    }

    val quartInOut: EasingFunction = { t ->
        if (t < 0.5) 8.0 * t * t * t * t
        else 1.0 - (-2.0 * t + 2.0).let { it * it * it * it } / 2.0
    }

    val quintInOut: EasingFunction = { t ->
        if (t < 0.5) 16.0 * t * t * t * t * t
        else 1.0 - (-2.0 * t + 2.0).let { it * it * it * it * it } / 2.0
    }

    // --- Trigonometric ---

    val sinIn: EasingFunction = { t -> 1.0 - cos(t * PI / 2.0) }
    val sinOut: EasingFunction = { t -> sin(t * PI / 2.0) }
    val sinInOut: EasingFunction = { t -> -(cos(PI * t) - 1.0) / 2.0 }

    // --- Exponential ---

    val expoIn: EasingFunction = { t -> if (t == 0.0) 0.0 else 2.0.pow(10.0 * t - 10.0) }
    val expoOut: EasingFunction = { t -> if (t == 1.0) 1.0 else 1.0 - 2.0.pow(-10.0 * t) }
    val expoInOut: EasingFunction = { t ->
        when {
            t == 0.0 -> 0.0
            t == 1.0 -> 1.0
            t < 0.5 -> 2.0.pow(20.0 * t - 10.0) / 2.0
            else -> (2.0 - 2.0.pow(-20.0 * t + 10.0)) / 2.0
        }
    }

    // --- Bounce ---

    val bounceOut: EasingFunction = { t ->
        when {
            t < 1.0 / 2.75 -> 7.5625 * t * t
            t < 2.0 / 2.75 -> { val u = t - 1.5 / 2.75; 7.5625 * u * u + 0.75 }
            t < 2.5 / 2.75 -> { val u = t - 2.25 / 2.75; 7.5625 * u * u + 0.9375 }
            else -> { val u = t - 2.625 / 2.75; 7.5625 * u * u + 0.984375 }
        }
    }

    val bounceIn: EasingFunction = { t -> 1.0 - bounceOut(1.0 - t) }

    val bounceInOut: EasingFunction = { t ->
        if (t < 0.5) (1.0 - bounceOut(1.0 - 2.0 * t)) / 2.0
        else (1.0 + bounceOut(2.0 * t - 1.0)) / 2.0
    }

    // --- Back (overshoot) ---

    fun backIn(strength: Double = 1.70158): EasingFunction = { t ->
        t * t * ((strength + 1.0) * t - strength)
    }

    fun backOut(strength: Double = 1.70158): EasingFunction = { t ->
        val u = t - 1.0
        u * u * ((strength + 1.0) * u + strength) + 1.0
    }

    fun backInOut(strength: Double = 1.70158): EasingFunction = { t ->
        val s = strength * 1.525
        if (t < 0.5) {
            val u = 2.0 * t
            (u * u * ((s + 1.0) * u - s)) / 2.0
        } else {
            val u = 2.0 * t - 2.0
            (u * u * ((s + 1.0) * u + s)) / 2.0 + 1.0
        }
    }

    // --- Elastic ---

    val elasticOut: EasingFunction = { t ->
        if (t == 0.0 || t == 1.0) t
        else 2.0.pow(-10.0 * t) * sin((t - 1.0) * (2.0 * PI) / 0.3) + 1.0
    }

    val elasticIn: EasingFunction = { t ->
        if (t == 0.0 || t == 1.0) t
        else -(2.0.pow(10.0 * (t - 1.0))) * sin((t - 1.0) * (2.0 * PI) / 0.3)
    }

    val elasticInOut: EasingFunction = { t ->
        if (t == 0.0 || t == 1.0) t
        else if (t < 0.5) {
            -(2.0.pow(20.0 * t - 10.0) * sin((20.0 * t - 11.125) * (2.0 * PI) / 4.5)) / 2.0
        } else {
            (2.0.pow(-20.0 * t + 10.0) * sin((20.0 * t - 11.125) * (2.0 * PI) / 4.5)) / 2.0 + 1.0
        }
    }

    // --- Custom ---

    fun fromPolynomial(poly: Polynomial): EasingFunction {
        val offset = poly(0.0)
        val scale = poly(1.0) - offset
        if (scale == 0.0) return { t -> t }
        return { t -> (poly(t) - offset) / scale }
    }
}