package org.soyuz.engine.shape

import org.soyuz.util.Vector2D

/**
 * Axis-aligned bounding box represented with scalar extents.
 *
 * The coordinate space is conveyed by API naming (for example, `localAabb` or `worldAabb`).
 */
data class Aabb2D(
    val minX: Double,
    val minY: Double,
    val maxX: Double,
    val maxY: Double
) {
    fun contains(point: Vector2D): Boolean =
        point.x in minX..maxX &&
            point.y in minY..maxY
}
