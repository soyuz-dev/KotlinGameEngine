package org.soyuz.engine.shape

import kotlin.math.sqrt

data class RectangleShape(
    val width: Double,
    val height: Double
) : Shape2D {

    init {
        require(width.isFinite()) { "RectangleShape width must be finite (not NaN or Infinity): $width" }
        require(height.isFinite()) { "RectangleShape height must be finite (not NaN or Infinity): $height" }
        require(width > 0f) { "RectangleShape width must be > 0: $width" }
        require(height > 0f) { "RectangleShape height must be > 0: $height" }
    }

    override fun localAabb(): Aabb2D {
        val halfWidth = width * 0.5
        val halfHeight = height * 0.5
        return Aabb2D(
            minX = -halfWidth,
            minY = -halfHeight,
            maxX = halfWidth,
            maxY = halfHeight
        )
    }

    override fun supportRadius(): Double {
        val halfWidth = width * 0.5f
        val halfHeight = height * 0.5f
        return sqrt(halfWidth * halfWidth + halfHeight * halfHeight)
    }
}
