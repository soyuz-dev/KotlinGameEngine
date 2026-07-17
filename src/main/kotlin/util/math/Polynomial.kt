package org.soyuz.util.math


/**
 * A simple polynomial utility
 * Coefficients are stored in ascending order of degree:
 * c₀ + c₁x + c₂x² + ...
 */
class Polynomial(vararg coeffs: Double) : (Double) -> Double {

    private val coefficients:DoubleArray = coeffs

    fun evaluate(x: Double): Double {
        var result = 0.0
        for (i in coefficients.indices.reversed()) {
            result = result * x + coefficients[i]
        }
        return result
    }

    override operator fun invoke(x: Double): Double = evaluate(x)

    operator fun plus(other: Polynomial): Polynomial {
        val maxLen = maxOf(coefficients.size, other.coefficients.size)
        val result = DoubleArray(maxLen)
        for (i in 0 until maxLen) {
            result[i] = (coefficients.getOrElse(i) { 0.0 } + other.coefficients.getOrElse(i) { 0.0 })
        }
        return Polynomial(*result)
    }

    operator fun minus(other: Polynomial): Polynomial {
        val maxLen = maxOf(coefficients.size, other.coefficients.size)
        val result = DoubleArray(maxLen)
        for (i in 0 until maxLen) {
            result[i] = (coefficients.getOrElse(i) { 0.0 } - other.coefficients.getOrElse(i) { 0.0 })
        }
        return Polynomial(*result)
    }

    operator fun times(scalar: Double): Polynomial {
        return Polynomial(*(coefficients.map { it * scalar }.toDoubleArray()))
    }

    operator fun times(other: Polynomial): Polynomial {
        val result = DoubleArray(coefficients.size + other.coefficients.size - 1)

        for (i in coefficients.indices) {
            for (j in other.coefficients.indices) {
                result[i + j] += coefficients[i] * other.coefficients[j]
            }
        }

        return Polynomial(*result)
    }


    operator fun div(scalar: Double): Polynomial {
        return Polynomial(*(coefficients.map { it / scalar }.toDoubleArray()))
    }

    fun derivative(): Polynomial {
        if (coefficients.size <= 1) return constant(0.0)

        val result = DoubleArray(coefficients.size - 1)
        for (i in 1 until coefficients.size) {
            result[i - 1] = coefficients[i] * i
        }
        return Polynomial(*result)
    }

    fun integral(constant: Double = 0.0): Polynomial {
        val result = DoubleArray(coefficients.size + 1)
        result[0] = constant
        for (i in coefficients.indices) {
            result[i + 1] = coefficients[i] / (i + 1)
        }
        return Polynomial(*result)
    }

    companion object {
        fun linear(a: Double, b: Double = 0.0) = Polynomial(b, a)
        fun quadratic(a: Double, b: Double = 0.0, c: Double = 0.0) = Polynomial(c, b, a)
        fun constant(value: Double) = Polynomial(value)
    }
}

operator fun Double.times(polynomial: Polynomial): Polynomial = polynomial * this