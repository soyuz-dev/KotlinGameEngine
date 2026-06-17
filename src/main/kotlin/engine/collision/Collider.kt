package org.soyuz.engine.collision

import org.soyuz.util.Transform
import org.soyuz.util.Vector2D

interface Collider {
    val isTrigger: Boolean

    fun intersects(other: Collider, selfTransform: Transform, otherTransform: Transform): Boolean

    fun boundingCircle(transform: Transform) : Pair<Vector2D, Double>
}
