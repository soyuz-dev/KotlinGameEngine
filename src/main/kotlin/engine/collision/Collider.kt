package org.soyuz.engine.collision

import org.soyuz.util.math.Transform
import org.soyuz.util.math.Vector2D

interface Collider {
    val isTrigger: Boolean

    fun intersects(other: Collider, selfTransform: Transform, otherTransform: Transform): Boolean

    fun boundingCircle(transform: Transform) : Pair<Vector2D, Double>

    fun containsPoint(point: Vector2D, transform: Transform): Boolean
}
