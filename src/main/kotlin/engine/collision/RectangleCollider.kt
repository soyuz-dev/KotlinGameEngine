package org.soyuz.engine.collision

import org.soyuz.util.Transform
import org.soyuz.engine.shape.RectangleShape
import org.soyuz.util.MathUtil
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
                val distSq = (otherTransform.position-closest).lengthSquared()
                val result = distSq <= radius * radius
                println("rect-circle: rectPos=${selfTransform.position}, circlePos=${otherTransform.position}, closest=$closest, distSq=$distSq, radius=$radius, result=$result")
                result
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
            MathUtil.clamp(local.x, -shape.width/2, shape.width/2),
            MathUtil.clamp(local.y, -shape.height/2, shape.height/2)
        )
        return Transform.localToWorld(clamped, transform)
    }



    fun worldCorners(transform: Transform): List<Vector2D> {
        val halfW = shape.width * 0.5
        val halfH = shape.height * 0.5
        return listOf(
            Transform.localToWorld(Vector2D(-halfW, -halfH), transform),
            Transform.localToWorld(Vector2D( halfW, -halfH), transform),
            Transform.localToWorld(Vector2D( halfW,  halfH), transform),
            Transform.localToWorld(Vector2D(-halfW,  halfH), transform),
        )
    }


    fun satForRectangle(other: RectangleCollider, selfTransform: Transform, otherTransform: Transform) : Boolean {
        val c1 = cos(selfTransform.rotationRadians)
        val s1 = sin(selfTransform.rotationRadians)


        val c2 = cos(otherTransform.rotationRadians)
        val s2 = sin(otherTransform.rotationRadians)




        val selfCorners = worldCorners(selfTransform)
        val otherCorners = other.worldCorners(otherTransform)

        val selfAxisX = (selfCorners[1] - selfCorners[0]).normalized()
        val selfAxisY = (selfCorners[3] - selfCorners[0]).normalized()

        val otherAxisX = (otherCorners[1] - otherCorners[0]).normalized()
        val otherAxisY = (otherCorners[3] - otherCorners[0]).normalized()

        val axes = listOf(selfAxisX, selfAxisY, otherAxisX, otherAxisY)

        for (axis in axes) {
            val (minA, maxA) = MathUtil.project(selfCorners, axis)
            val (minB, maxB) = MathUtil.project(otherCorners, axis)
            val overlap = minOf(maxA, maxB) - maxOf(minA, minB)
            println("axis: $axis, minA=$minA, maxA=$maxA, minB=$minB, maxB=$maxB, overlap=$overlap")
            if (overlap <= 0) return false // separated on this axis
        }
        return true
    }

}