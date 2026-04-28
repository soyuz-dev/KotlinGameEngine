package org.soyuz.engine.shape

import kotlin.math.sqrt

sealed interface Shape2D {
    fun localAabb(): Aabb2D
    fun supportRadius(): Float
}

data class CircleShape(
    val radius: Float
) : Shape2D {
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
