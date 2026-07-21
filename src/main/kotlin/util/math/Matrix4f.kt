package org.soyuz.util.math


import kotlin.math.cos
import kotlin.math.sin

data class Matrix4f(
    private val data: FloatArray
) {
    init {
        require(data.size == 16) {
            "Matrix4f requires exactly 16 elements"
        }
    }

    constructor() : this(floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f
    ))

    operator fun get(row: Int, col: Int): Float =
        data[col * 4 + row]

    operator fun times(other: Matrix4f): Matrix4f {
        val result = FloatArray(16)

        for (col in 0..3) {
            for (row in 0..3) {
                var sum = 0f
                for (k in 0..3) {
                    sum += this[row, k] * other[k, col]
                }
                result[col * 4 + row] = sum
            }
        }

        return Matrix4f(result)
    }

    operator fun times(scalar: Float): Matrix4f =
        Matrix4f(FloatArray(16) { data[it] * scalar })


    fun translate(x: Float, y: Float): Matrix4f =
        this * translateMatrix(x, y)

    fun scale(x: Float, y: Float): Matrix4f =
        this * scaleMatrix(x, y)

    fun rotate(radians: Float): Matrix4f =
        this * rotateMatrix(radians)


    fun toFloatArray(): FloatArray =
        data.copyOf()


    companion object {

        fun identity() = Matrix4f()

        fun ortho(width: Float, height: Float): Matrix4f {
            return Matrix4f(floatArrayOf(
                2f / width, 0f, 0f, 0f,
                0f, -2f / height, 0f, 0f,
                0f, 0f, -1f, 0f,
                0f, 0f, 0f, 1f
            ))
        }
        fun translateMatrix(x: Float, y: Float) =
            Matrix4f(floatArrayOf(
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, 1f, 0f,
                x, y, 0f, 1f
            ))

        fun scaleMatrix(x: Float, y: Float) =
            Matrix4f(floatArrayOf(
                x, 0f, 0f, 0f,
                0f, y, 0f, 0f,
                0f, 0f, 1f, 0f,
                0f, 0f, 0f, 1f
            ))

        fun rotateMatrix(radians: Float): Matrix4f {
            val c = cos(radians)
            val s = sin(radians)

            return Matrix4f(floatArrayOf(
                c, s, 0f, 0f,
                -s, c, 0f, 0f,
                0f, 0f, 1f, 0f,
                0f, 0f, 0f, 1f
            ))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Matrix4f
        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}