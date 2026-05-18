package org.soyuz.engine.collision

import org.soyuz.util.Transform
import org.soyuz.engine.shape.RectangleShape
import org.soyuz.util.Vector2D
import kotlin.math.cos
import kotlin.math.sin

class RectangleCollider(
    val shape: RectangleShape,
    override val isTrigger: Boolean = false
) : Collider {
    override fun intersects(other: Collider, selfTransform: Transform, otherTransform: Transform): Boolean {
        return when(other) {
            is CircleCollider -> {
                val closest = closestPointTo(otherTransform.position, selfTransform)
                val radius = other.worldRadius(otherTransform)
                return (otherTransform.position - closest).lengthSquared() <= radius * radius
            }
            is RectangleCollider -> {
                return satForRectangle(other, selfTransform, otherTransform)
            }
            else -> other.intersects(this, otherTransform, selfTransform)
        }

    }

    fun closestPointTo(worldPoint: Vector2D, transform: Transform) : Vector2D {
        val local = Transform.worldToLocal(worldPoint, transform) ?: return transform.position
        val clamped = Vector2D(
           clamp(local.x, -shape.width/2, shape.width/2),
            clamp(local.y, -shape.height/2, shape.height/2)
        )
        return Transform.localToWorld(clamped, transform)
    }


    fun clamp(toClamp:Double, min: Double, max: Double) : Double {
        return if (toClamp > max) max else if (toClamp < min) min else toClamp
    }

    private fun worldCorners(transform: Transform): List<Vector2D> {
        val halfW = shape.width * 0.5
        val halfH = shape.height * 0.5
        return listOf(
            Transform.localToWorld(Vector2D(-halfW, -halfH), transform),
            Transform.localToWorld(Vector2D( halfW, -halfH), transform),
            Transform.localToWorld(Vector2D( halfW,  halfH), transform),
            Transform.localToWorld(Vector2D(-halfW,  halfH), transform),
        )
    }

    private fun project(corners: List<Vector2D>, axis: Vector2D): Pair<Double, Double> {
        var min = Double.MAX_VALUE
        var max = -Double.MAX_VALUE
        for (corner in corners) {
            val projection = corner.dot(axis)
            if (projection < min) min = projection
            if (projection > max) max = projection
        }
        return min to max
    }

    fun satForRectangle(other: RectangleCollider, selfTransform: Transform, otherTransform: Transform) : Boolean {
        val c1 = cos(selfTransform.rotationRadians)
        val s1 = sin(selfTransform.rotationRadians)

        val selfAxisX = Vector2D(c1, s1)        // (1,0) rotated
        val selfAxisY = Vector2D(-s1, c1)       // (0,1) rotated

        val c2 = cos(otherTransform.rotationRadians)
        val s2 = sin(otherTransform.rotationRadians)

        val otherAxisX = Vector2D(c2, s2)
        val otherAxisY = Vector2D(-s2, c2)

        val axes = listOf(selfAxisX, selfAxisY, otherAxisX, otherAxisY)

        val selfCorners = worldCorners(selfTransform)
        val otherCorners = worldCorners(otherTransform)

        for (axis in axes) {
            val (minA, maxA) = project(selfCorners, axis)
            val (minB, maxB) = project(otherCorners, axis)
            val overlap = minOf(maxA, maxB) - maxOf(minA, minB)
            if (overlap <= 0) return false // separated on this axis
        }
        return true
    }

}