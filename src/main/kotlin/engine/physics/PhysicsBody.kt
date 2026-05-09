package org.soyuz.engine.physics

import org.soyuz.util.Vector2D
interface PhysicsBody {
    var mass: Double
    var velocity: Vector2D
    fun applyForce(force: Vector2D)
    fun integratePosition(dt: Double): Vector2D = Vector2D.ZERO
    fun integrateVelocity(dt: Double, newAcceleration: Vector2D? = null) = Unit
}

