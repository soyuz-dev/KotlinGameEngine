package org.soyuz.engine.physics

import org.soyuz.util.Vector2D

interface PhysicsBody {
    var isStatic: Boolean

    var mass: Float

    var velocity: Vector2D

    fun applyForce(force: Vector2D)

    fun setVelocity(x: Float, y: Float)
}
