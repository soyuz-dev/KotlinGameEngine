package org.soyuz.engine.shape

data class CircleShape(
    val radius: Double
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

    override fun supportRadius(): Double = radius
}
