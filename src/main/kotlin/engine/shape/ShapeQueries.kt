package org.soyuz.engine.shape

import org.soyuz.engine.entity.Transform
import org.soyuz.util.Vector2D
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Utility entry-point for projecting local [Shape2D] geometry into world-space.
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
        }
    /**
     * Tests a world-space [point] against a local [shape] projected by [transform].
     */
    fun containsPoint(shape: Shape2D, transform: Transform, point: Vector2D): Boolean {
        val localPoint = worldToLocal(point, transform) ?: return false
        return when (shape) {
            is CircleShape -> localPoint.lengthSquared() <= shape.radius.toDouble() * shape.radius
            is RectangleShape -> {
                val halfWidth = shape.width * 0.5
                val halfHeight = shape.height * 0.5
                localPoint.x in -halfWidth..halfWidth && localPoint.y in -halfHeight..halfHeight
            }
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
        val corner0 = localToWorld(Vector2D(-halfWidth, -halfHeight), transform)
        val corner1 = localToWorld(Vector2D(halfWidth, -halfHeight), transform)
        val corner2 = localToWorld(Vector2D(halfWidth, halfHeight), transform)
        val corner3 = localToWorld(Vector2D(-halfWidth, halfHeight), transform)

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

    private fun localToWorld(local: Vector2D, transform: Transform): Vector2D {
        val scaled = Vector2D(local.x * transform.scale.x, local.y * transform.scale.y)
        val rotated = scaled.rotate(transform.rotationRadians)
        return rotated + transform.position
    }

    private fun worldToLocal(world: Vector2D, transform: Transform): Vector2D? {
        if (abs(transform.scale.x) < Vector2D.EPSILON_NORMALIZE ||
            abs(transform.scale.y) < Vector2D.EPSILON_NORMALIZE
        ) {
            return null
        }

        val translated = world - transform.position
        val unrotated = translated.rotate(-transform.rotationRadians)
        return Vector2D(unrotated.x / transform.scale.x, unrotated.y / transform.scale.y)
    }
}
