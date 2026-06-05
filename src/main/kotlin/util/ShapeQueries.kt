package org.soyuz.util

import org.soyuz.util.Transform
import org.soyuz.engine.shape.Aabb2D
import org.soyuz.engine.shape.CircleShape
import org.soyuz.engine.shape.RectangleShape
import org.soyuz.engine.shape.Shape2D
import org.soyuz.engine.shape.TriangleShape
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Utility entry-point for projecting local [org.soyuz.engine.shape.Shape2D] geometry into world-space.
 *
 * This keeps shapes runtime-agnostic and ensures broadphase queries can go through one path.
 */
object ShapeQueries {
    /**
     * Builds a world-space AABB by projecting local bounds through [transform].
     */
    fun worldAabb(shape: Shape2D, transform: Transform): Aabb2D =
        when (shape) {
            is CircleShape -> worldAabbForCircle(shape, transform)
            is RectangleShape -> worldAabbForRectangle(shape, transform)
            is TriangleShape -> worldAabbForTriangle(shape, transform)
        }
    /**
     * Tests a world-space [point] against a local [shape] projected by [transform].
     */
    fun containsPoint(shape: Shape2D, transform: Transform, point: Vector2D): Boolean {
        val localPoint = Transform.worldToLocal(point, transform) ?: return false
        return when (shape) {
            is CircleShape -> localPoint.lengthSquared() <= shape.radius.toDouble() * shape.radius
            is RectangleShape -> {
                val halfWidth = shape.width * 0.5
                val halfHeight = shape.height * 0.5
                localPoint.x in -halfWidth..halfWidth && localPoint.y in -halfHeight..halfHeight
            }
            is TriangleShape -> pointInTriangle(point, shape.a, shape.b, shape.c)
        }
    }
    private fun worldAabbForCircle(shape: CircleShape, transform: Transform): Aabb2D {
        val center = transform.position
        val radius = shape.radius.toDouble()
        val scaledX = radius * abs(transform.scale.x)
        val scaledY = radius * abs(transform.scale.y)

        val c = cos(transform.rotationRadians)
        val s = sin(transform.rotationRadians)

        // Circle with non-uniform scale becomes an ellipse before rotation.
        val extentX = sqrt((scaledX * c) * (scaledX * c) + (scaledY * s) * (scaledY * s))
        val extentY = sqrt((scaledX * s) * (scaledX * s) + (scaledY * c) * (scaledY * c))

        return Aabb2D(
            minX = (center.x - extentX),
            minY = (center.y - extentY),
            maxX = (center.x + extentX),
            maxY = (center.y + extentY)
        )
    }

    private fun worldAabbForRectangle(shape: RectangleShape, transform: Transform): Aabb2D {
        val halfWidth = shape.width * 0.5
        val halfHeight = shape.height * 0.5
        val corner0 = Transform.localToWorld(Vector2D(-halfWidth, -halfHeight), transform)
        val corner1 = Transform.localToWorld(Vector2D(halfWidth, -halfHeight), transform)
        val corner2 = Transform.localToWorld(Vector2D(halfWidth, halfHeight), transform)
        val corner3 = Transform.localToWorld(Vector2D(-halfWidth, halfHeight), transform)

        var minX = corner0.x
        var minY = corner0.y
        var maxX = corner0.x
        var maxY = corner0.y

        if (corner1.x < minX) minX = corner1.x
        if (corner1.y < minY) minY = corner1.y
        if (corner1.x > maxX) maxX = corner1.x
        if (corner1.y > maxY) maxY = corner1.y

        if (corner2.x < minX) minX = corner2.x
        if (corner2.y < minY) minY = corner2.y
        if (corner2.x > maxX) maxX = corner2.x
        if (corner2.y > maxY) maxY = corner2.y

        if (corner3.x < minX) minX = corner3.x
        if (corner3.y < minY) minY = corner3.y
        if (corner3.x > maxX) maxX = corner3.x
        if (corner3.y > maxY) maxY = corner3.y

        return Aabb2D(
            minX = minX,
            minY = minY,
            maxX = maxX,
            maxY = maxY
        )
    }

    private fun worldAabbForTriangle(shape: TriangleShape, transform: Transform): Aabb2D {
        val a = Transform.localToWorld(shape.a, transform)
        val b = Transform.localToWorld(shape.b, transform)
        val c = Transform.localToWorld(shape.c, transform)
        return Aabb2D(
            minX = minOf(a.x, b.x, c.x),
            minY = minOf(a.y, b.y, c.y),
            maxX = maxOf(a.x, b.x, c.x),
            maxY = maxOf(a.y, b.y, c.y)
        )
    }

    private fun pointInTriangle(p: Vector2D, a: Vector2D, b: Vector2D, c: Vector2D): Boolean {
        val v0 = c - a
        val v1 = b - a
        val v2 = p - a
        val dot00 = v0.dot(v0)
        val dot01 = v0.dot(v1)
        val dot02 = v0.dot(v2)
        val dot11 = v1.dot(v1)
        val dot12 = v1.dot(v2)
        val invDenom = 1.0 / (dot00 * dot11 - dot01 * dot01)
        val u = (dot11 * dot02 - dot01 * dot12) * invDenom
        val v = (dot00 * dot12 - dot01 * dot02) * invDenom
        return u >= 0.0 && v >= 0.0 && u + v <= 1.0
    }


}