package org.soyuz.engine.shape

sealed interface Shape2D

data class CircleShape(
    val radius: Float
) : Shape2D {
    init {
        require(radius.isFinite()) { "CircleShape radius must be finite (not NaN or Infinity): $radius" }
        require(radius > 0f) { "CircleShape radius must be > 0: $radius" }
    }
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
}
