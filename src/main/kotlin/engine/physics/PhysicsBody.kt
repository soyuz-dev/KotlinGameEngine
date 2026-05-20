package org.soyuz.engine.physics

import org.soyuz.util.Vector2D

interface PhysicsBody {
    var mass: Double

    val restitution:Double
    var velocity: Vector2D
    fun applyForce(force: Vector2D) = Unit
    fun applyImpulse(impulse: Vector2D, contactPoint: Vector2D) = Unit
    fun integratePosition(dt: Double): Vector2D = Vector2D.ZERO
    fun integrateVelocity(dt: Double, newAcceleration: Vector2D? = null) = Unit
}