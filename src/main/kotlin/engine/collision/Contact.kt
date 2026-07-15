package org.soyuz.engine.collision

import org.soyuz.util.math.Vector2D

data class Contact(
    val entityA: String,
    val entityB: String,
    val point: Vector2D,      // world-space contact point
    val normal: Vector2D,     // collision normal, pointing from A toward B
    val depth: Double         // penetration depth (positive = overlapping)
) {
    fun flipped(): Contact = Contact(
        entityA = entityB,
        entityB = entityA,
        point = point,
        normal = -normal,
        depth = depth
    )
}