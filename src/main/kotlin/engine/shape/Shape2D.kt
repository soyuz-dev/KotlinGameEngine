package org.soyuz.engine.shape

import kotlin.math.sqrt

sealed interface Shape2D {
    fun localAabb(): Aabb2D
    fun supportRadius(): Float
}

data class CircleShape(
    val radius: Float
) : Shape2D {

    init {
        require(radius.isFinite()) { "CircleShape radius must be finite (not NaN or Infinity): $radius" }
        require(radius > 0f) { "CircleShape radius must be > 0: $radius" }
    }

    override fun localAabb(): Aabb2D = Aabb2D(
        minX = -radius,
        minY = -radius,
        maxX = radius,
        maxY = radius
    )

    override fun supportRadius(): Float = radius

}

data class RectangleShape(
    val width: Float,
    val height: Float
) : Shape2D {

    init {
        require(width.isFinite()) { "RectangleShape width must be finite (not NaN or Infinity): $width" }
        require(height.isFinite()) { "RectangleShape height must be finite (not NaN or Infinity): $height" }
        require(width > 0f) { "RectangleShape width must be > 0: $width" }
        require(height > 0f) { "RectangleShape height must be > 0: $height" }
    }

    override fun localAabb(): Aabb2D {
        val halfWidth = width * 0.5f
        val halfHeight = height * 0.5f
        return Aabb2D(
            minX = -halfWidth,
            minY = -halfHeight,
            maxX = halfWidth,
            maxY = halfHeight
        )
    }

    override fun supportRadius(): Float {
        val halfWidth = width * 0.5f
        val halfHeight = height * 0.5f
        return sqrt(halfWidth * halfWidth + halfHeight * halfHeight)
    }
}
