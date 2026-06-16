package org.soyuz.engine.physics

import org.soyuz.engine.physics.forcefields.ForceField
import org.soyuz.util.Vector2D

class RigidBody(
    mass: Double = 1.0,
    val width: Double = 1.0,
    val height: Double = 1.0,
    override val restitution: Double = 0.3,
    val friction: Double = 0.2
) : PhysicsBody {

    private val pointMass = PointMass(mass)

    // Delegate linear physics to PointMass
    override var mass: Double
        get() = pointMass.mass
        set(value) {
            pointMass.mass = value
            inverseInertia = if (value > 0.0) {
                val I = (value * (width * width + height * height)) / 12.0
                1.0 / I
            } else 0.0
        }

    var inverseMass: Double = pointMass.inverseMass
        private set
        get() = pointMass.inverseMass

    override var velocity: Vector2D
        get() = pointMass.velocity
        set(value) { pointMass.velocity = value }

    // Angular motion
    var angularVelocity: Double = 0.0
    var torque: Double = 0.0
    var inverseInertia: Double = if (mass > 0.0) {
        val momentOfInertia = (mass * (width * width + height * height)) / 12.0
        1.0 / momentOfInertia
    } else 0.0
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
        angularVelocity *= 0.99
        torque = 0.0
    }

    fun angularDisplacement(dt: Double): Double {
        return angularVelocity * dt
    }

    override fun addField(field: ForceField) {
        pointMass.addField(field)
    }

    override fun removeField(field: ForceField) {
        pointMass.removeField(field)
    }

    fun accumulateForces(position: Vector2D) = pointMass.accumulateForces(position)
}