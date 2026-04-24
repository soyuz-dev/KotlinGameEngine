package org.soyuz.engine.collision

import org.soyuz.engine.entity.Transform

interface Collider {
    val isTrigger: Boolean

    fun intersects(other: Collider, selfTransform: Transform, otherTransform: Transform): Boolean
}
