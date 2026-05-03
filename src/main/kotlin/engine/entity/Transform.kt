package org.soyuz.engine.entity

import org.soyuz.util.Vector2D

data class Transform(
    val position: Vector2D = Vector2D.ZERO,
    val rotationRadians: Double = 0.0,
    val scale: Vector2D = Vector2D(1.0, 1.0)
) {
    init {
        require(rotationRadians.isFinite()) { "rotationRadians must be finite: $rotationRadians" }
        require(position.x.isFinite() && position.y.isFinite()) { "position must be finite: $position" }
        require(scale.x.isFinite() && scale.y.isFinite()) { "scale must be finite: $scale" }
    }

    fun translated(delta: Vector2D): Transform =
        copy(position = position + delta)

    fun rotated(deltaRadians: Double): Transform =
        copy(rotationRadians = rotationRadians + deltaRadians)

    fun scaled(factor: Double): Transform =
        copy(scale = scale * factor)

    fun scaled(factor: Vector2D): Transform =
        copy(scale = Vector2D(scale.x * factor.x, scale.y * factor.y))
}