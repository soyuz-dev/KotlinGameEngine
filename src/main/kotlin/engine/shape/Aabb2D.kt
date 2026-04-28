package org.soyuz.engine.shape

/**
 * Axis-aligned bounding box in local space.
 */
data class Aabb2D(
    val minX: Float,
    val minY: Float,
    val maxX: Float,
    val maxY: Float
)
