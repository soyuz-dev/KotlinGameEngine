package org.soyuz.engine.physics

import org.soyuz.engine.physics.forcefields.ForceField
import org.soyuz.util.math.Vector2D

interface PhysicsBody {
    var mass: Double
    val restitution:Double
    var velocity: Vector2D
    fun applyForce(force: Vector2D) = Unit
    fun applyImpulse(impulse: Vector2D, contactPoint: Vector2D) = Unit
    fun integratePosition(dt: Double): Vector2D = Vector2D.ZERO
    fun integrateVelocity(dt: Double, newAcceleration: Vector2D? = null) = Unit
    fun addField(field: ForceField) = Unit
    fun removeField(field: ForceField) = Unit
}

infix fun <T : PhysicsBody> T.with(field: ForceField): T {
    this.addField(field)
    return this
}