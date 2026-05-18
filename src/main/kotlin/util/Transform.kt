package org.soyuz.util

import kotlin.math.abs

//screw floats
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

    fun goto(position: Vector2D): Transform =
        copy(position = position)

    companion object {
        fun localToWorld(local: Vector2D, transform: Transform): Vector2D {
            val scaled = Vector2D(local.x * transform.scale.x, local.y * transform.scale.y)
            val rotated = scaled.rotate(transform.rotationRadians)
            return rotated + transform.position
        }

        fun worldToLocal(world: Vector2D, transform: Transform): Vector2D? {
            if (abs(transform.scale.x) < Vector2D.EPSILON_NORMALIZE ||
                abs(transform.scale.y) < Vector2D.EPSILON_NORMALIZE
            ) {
                return null
            }

            val translated = world - transform.position
            val unrotated = translated.rotate(-transform.rotationRadians)
            return Vector2D(unrotated.x / transform.scale.x, unrotated.y / transform.scale.y)
        }
    }
}