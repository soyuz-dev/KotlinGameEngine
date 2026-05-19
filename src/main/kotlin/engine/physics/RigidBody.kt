package org.soyuz.engine.physics

import org.soyuz.util.Vector2D

class RigidBody(
    mass: Double = 1.0,
    val restitution: Double = 0.3,
    val friction: Double = 0.2
) : PhysicsBody {

    private val pointMass = PointMass(mass)

    // Delegate linear physics to PointMass
    override var mass: Double
        get() = pointMass.mass
        set(value) {
            pointMass.mass = value
            inverseInertia = 1/value
        }

    override var velocity: Vector2D
        get() = pointMass.velocity
        set(value) { pointMass.velocity = value }

    // Angular motion
    var angularVelocity: Double = 0.0
    var torque: Double = 0.0
    var inverseInertia: Double = if (mass > 0.0) 1.0 / mass else 0.0
        private set

    override fun applyForce(force: Vector2D) {
        pointMass.applyForce(force)
    }



    override fun applyImpulse(impulse: Vector2D, contactPoint: Vector2D) {
        pointMass.applyImpulse(impulse)

        val angularImpulse = contactPoint.cross(impulse)
        angularVelocity += angularImpulse * inverseInertia
    }

    fun applyAngularImpulse(angularImpulse: Double) {
        angularVelocity += angularImpulse * inverseInertia
    }

    fun applyTorque(t: Double) {
        torque += t
    }

    override fun integratePosition(dt: Double): Vector2D {
        return pointMass.integratePosition(dt)
    }

    override fun integrateVelocity(dt: Double, newAcceleration: Vector2D?) {
        pointMass.integrateVelocity(dt, newAcceleration)
        angularVelocity += torque * inverseInertia * dt
        torque = 0.0
    }

    fun angularDisplacement(dt: Double): Double {
        return angularVelocity * dt
    }
}