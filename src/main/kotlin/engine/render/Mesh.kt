package org.soyuz.engine.render

import org.lwjgl.opengl.GL30.*
import org.soyuz.util.Vector2D
import kotlin.math.cos
import kotlin.math.sin

class Mesh(vertices: FloatArray, private val mode: Int) {
    private val vao: Int
    private val vbo: Int
    private val vertexCount: Int = vertices.size / 2

    init {
        vao = glGenVertexArrays()
        glBindVertexArray(vao)

        vbo = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0)
        glEnableVertexAttribArray(0)

        glBindVertexArray(0)
    }

    fun draw() {
        glBindVertexArray(vao)
        glDrawArrays(mode, 0, vertexCount)
        glBindVertexArray(0)
    }

    fun cleanup() {
        glDeleteBuffers(vbo)
        glDeleteVertexArrays(vao)
    }

    companion object {
        fun quad(): Mesh = Mesh(
            floatArrayOf(
                -0.5f, -0.5f,
                0.5f, -0.5f,
                -0.5f,  0.5f,
                0.5f,  0.5f
            ),
            GL_TRIANGLE_STRIP
        )

        fun circle(segments: Int = 32): Mesh {
            val verts = FloatArray((segments + 2) * 2)
            verts[0] = 0f; verts[1] = 0f // center for fan
            for (i in 0..segments) {
                val angle = 2.0 * Math.PI * i / segments
                verts[(i + 1) * 2]     = (cos(angle) * 0.5).toFloat()
                verts[(i + 1) * 2 + 1] = (sin(angle) * 0.5).toFloat()
            }
            return Mesh(verts, GL_TRIANGLE_FAN)
        }

        fun triangle(a: Vector2D, b: Vector2D, c: Vector2D): Mesh = Mesh(
            floatArrayOf(
                a.x.toFloat(), a.y.toFloat(),
                b.x.toFloat(), b.y.toFloat(),
                c.x.toFloat(), c.y.toFloat()
            ),
            GL_TRIANGLES
        )

        // Or a unit equilateral triangle centered at origin:
        fun equilateralTriangle(): Mesh {
            val h = 0.5f
            val w = (h / kotlin.math.sqrt(3.0)).toFloat()
            return Mesh(
                floatArrayOf(
                    0f, h,
                    -w, -h,
                    w, -h
                ),
                GL_TRIANGLES
            )
        }
    }
}