package org.soyuz.engine.shape

import org.soyuz.util.Vector2D

/**
 * Axis-aligned bounding box represented with scalar extents.
 *
 * The coordinate space is conveyed by API naming (for example, `localAabb` or `worldAabb`).
 */
data class Aabb2D(
    val minX: Float,
    val minY: Float,
    val maxX: Float,
    val maxY: Float
) {
    fun contains(point: Vector2D): Boolean =
        point.x in minX.toDouble()..maxX.toDouble() &&
            point.y in minY.toDouble()..maxY.toDouble()
}
