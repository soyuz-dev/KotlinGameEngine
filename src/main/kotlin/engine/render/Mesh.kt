package org.soyuz.engine.render

import org.lwjgl.opengl.GL30.*
import org.soyuz.util.Vector2D
import kotlin.math.cos
import kotlin.math.sin

class Mesh(
    vertices: FloatArray,
    private val mode: Int,
    private val hasUVs: Boolean = false
) {
    private val vao: Int
    private val vbo: Int
    private val vertexCount: Int
    private val stride: Int = if (hasUVs) 4 * 4 else 2 * 4 // 4 floats vs 2 floats, each 4 bytes

    init {
        vertexCount = vertices.size / (if (hasUVs) 4 else 2)

        vao = glGenVertexArrays()
        glBindVertexArray(vao)

        vbo = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        // Position attribute (location 0)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0)
        glEnableVertexAttribArray(0)

        if (hasUVs) {
            // UV attribute (location 1)
            glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 2 * 4)
            glEnableVertexAttribArray(1)
        }

        glBindVertexArray(0)
    }

    fun draw() {
        glBindVertexArray(vao)
        glDrawArrays(mode, 0, vertexCount)
        glBindVertexArray(0)
    }

    fun updateVertices(vertices: FloatArray) {
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices)
    }

    fun cleanup() {
        glDeleteBuffers(vbo)
        glDeleteVertexArrays(vao)
    }

    companion object {
        fun quad(): Mesh = Mesh(
            floatArrayOf(
                // positions     // uvs
                -0.5f, -0.5f,    0f, 0f,
                0.5f, -0.5f,    1f, 0f,
                -0.5f,  0.5f,    0f, 1f,
                0.5f,  0.5f,    1f, 1f
            ),
            GL_TRIANGLE_STRIP,
            hasUVs = true
        )

        fun circle(segments: Int = 32): Mesh {
            val verts = FloatArray((segments + 2) * 2) // no UVs for circles
            verts[0] = 0f; verts[1] = 0f
            for (i in 0..segments) {
                val angle = 2.0 * Math.PI * i / segments
                verts[(i + 1) * 2]     = (cos(angle) * 0.5).toFloat()
                verts[(i + 1) * 2 + 1] = (sin(angle) * 0.5).toFloat()
            }
            return Mesh(verts, GL_TRIANGLE_FAN, hasUVs = false)
        }

        fun triangle(a: Vector2D, b: Vector2D, c: Vector2D): Mesh = Mesh(
            floatArrayOf(
                a.x.toFloat(), a.y.toFloat(),
                b.x.toFloat(), b.y.toFloat(),
                c.x.toFloat(), c.y.toFloat()
            ),
            GL_TRIANGLES,
            hasUVs = false
        )

        fun line(from: Vector2D, to: Vector2D): Mesh = Mesh(
            floatArrayOf(from.x.toFloat(), from.y.toFloat(), to.x.toFloat(), to.y.toFloat()),
            GL_LINES,
            hasUVs = false
        )
    }
}