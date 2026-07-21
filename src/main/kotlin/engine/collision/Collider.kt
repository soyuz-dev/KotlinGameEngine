package org.soyuz.engine.collision

import org.soyuz.engine.shape.CircleShape
import org.soyuz.engine.shape.RectangleShape
import org.soyuz.engine.shape.Shape2D
import org.soyuz.engine.shape.TriangleShape
import org.soyuz.util.math.Transform
import org.soyuz.util.math.Vector2D

interface Collider {
    val isTrigger: Boolean

    fun intersects(other: Collider, selfTransform: Transform, otherTransform: Transform): Boolean

    fun boundingCircle(transform: Transform) : Pair<Vector2D, Double>

    fun containsPoint(point: Vector2D, transform: Transform): Boolean

    companion object {
        operator fun invoke(shape: Shape2D, isTrigger: Boolean = false): Collider = when (shape) {
            is CircleShape -> CircleCollider(shape, isTrigger)
            is RectangleShape -> RectangleCollider(shape, isTrigger)
            is TriangleShape -> TriangleCollider(shape, isTrigger)
            else -> throw IllegalArgumentException("Unknown shape type: ${shape::class}")
        }
    }
}
