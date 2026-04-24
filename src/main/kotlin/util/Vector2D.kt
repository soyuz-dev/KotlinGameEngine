package org.soyuz.util
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector2D(val x: Double, val y: Double) {

    companion object {
        // Common constants
        val ZERO = Vector2D(0.0, 0.0)
        val UNIT_X = Vector2D(1.0, 0.0)
        val UNIT_Y = Vector2D(0.0, 1.0)

        const val EPSILON_ZERO = 1e-8      // for "is this basically zero?"
        const val EPSILON_COMPARE = 1e-6   // for equality checks
        const val EPSILON_NORMALIZE = 1e-12 // for avoiding divide-by-zero

    }

    // Basic arithmetic
    operator fun plus(other: Vector2D): Vector2D =
        Vector2D(x + other.x, y + other.y)

    operator fun minus(other: Vector2D): Vector2D =
        Vector2D(x - other.x, y - other.y)

    operator fun unaryMinus(): Vector2D =
        Vector2D(-x, -y)

    operator fun times(scalar: Double): Vector2D =
        Vector2D(x * scalar, y * scalar)

    operator fun div(scalar: Double): Vector2D =
        if (abs(scalar) > EPSILON_NORMALIZE) {
            Vector2D(x / scalar, y / scalar)
        } else {
            println("Division by zero detected. Did you intend scalar to be zero?")
            ZERO
        }

    // Length & normalization
    fun length(): Double =
        sqrt(x * x + y * y)

    fun lengthSquared(): Double =
        x * x + y * y

    fun normalize(): Vector2D {
        val len = length()
        if (len < EPSILON_NORMALIZE) return ZERO
        val invLen = 1.0 / len
        return Vector2D(x * invLen, y * invLen)
    }

    // Dot & cross products
    fun dot(other: Vector2D): Double =
        x * other.x + y * other.y

    /**
     * 2D "cross" product — returns scalar z-component magnitude
     * of the 3D cross product.
     */
    fun cross(other: Vector2D): Double =
        x * other.y - y * other.x

    /**
     * Returns a vector perpendicular to this one (rotated 90° CCW).
     */
    fun perpendicular(): Vector2D =
        Vector2D(-y, x)

    // Distance
    fun distance(to: Vector2D): Double =
        (this - to).length()

    // Projection and reflection
    fun project(onto: Vector2D): Vector2D {
        val norm = onto.normalize()
        return norm * (dot(norm))
    }

    fun reflect(normal: Vector2D): Vector2D {
        // v' = v - 2*(v·n)*n
        val d = dot(normal)
        return this - (normal * (2.0 * d))
    }

    // Rotation
    fun rotate(radians: Double): Vector2D {
        val c = cos(radians)
        val s = sin(radians)
        return Vector2D(
            x * c - y * s,
            x * s + y * c
        )
    }

    fun lerp(to: Vector2D, t: Double): Vector2D =
        this + (to - this) * t

    fun clamp(maxLength: Double): Vector2D {
        val lenSq = lengthSquared()
        if (lenSq > maxLength * maxLength) {
            return normalize() * maxLength
        }
        return this
    }

    fun isZero(): Boolean =
        abs(x) < EPSILON_ZERO && abs(y) < EPSILON_ZERO

    fun isCloseTo(other: Vector2D): Boolean {
        if (this === other) return true

        return abs(x - other.x) < EPSILON_COMPARE &&
                abs(y - other.y) < EPSILON_COMPARE
    }


    override fun toString(): String =
        "Vector2D[$x, $y]"
}

operator fun Double.times(v: Vector2D): Vector2D =
    Vector2D(this * v.x, this * v.y)