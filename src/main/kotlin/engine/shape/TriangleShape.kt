package org.soyuz.engine.shape

import org.soyuz.engine.render.Mesh
import org.soyuz.util.math.Vector2D

data class TriangleShape(
    val a: Vector2D,
    val b: Vector2D,
    val c: Vector2D
) : Shape2D {

    val mesh by lazy { Mesh.triangle(a, b, c) }

    override fun localAabb(): Aabb2D {
        val minX = minOf(a.x, b.x, c.x)
        val minY = minOf(a.y, b.y, c.y)
        val maxX = maxOf(a.x, b.x, c.x)
        val maxY = maxOf(a.y, b.y, c.y)
        return Aabb2D(minX, minY, maxX, maxY)
    }

    override fun boundingRadius(): Double {
        val center = Vector2D((a.x + b.x + c.x) / 3, (a.y + b.y + c.y) / 3)
        return maxOf(
            (a - center).length(),
            (b - center).length(),
            (c - center).length()
        )
    }

    companion object {

        private const val ROOT3 = 1.73205080757

        fun equilateral(length: Double): TriangleShape {
            val half = length / 2.0
            val height = length * ROOT3 / 2.0
            return TriangleShape(
                Vector2D(0.0, height * 2.0 / 3.0),           // top vertex (centered)
                Vector2D(-half, -height / 3.0),              // bottom-left
                Vector2D(half, -height / 3.0)                // bottom-right
            )
        }

        fun isosceles(width: Double, height: Double): TriangleShape {
            val halfW = width / 2.0
            val centroidY = height / 3.0  // centroid of isosceles triangle from base
            return TriangleShape(
                Vector2D(0.0, height - centroidY),     // nose (top)
                Vector2D(-halfW, -centroidY),          // bottom-left
                Vector2D(halfW, -centroidY)            // bottom-right
            )
        }
    }

}