package org.soyuz.engine.physics.forcefields

import org.soyuz.engine.physics.PhysicsBody
import org.soyuz.util.Polynomial
import org.soyuz.util.Vector2D

class VelocityForceField(private val polynomial: Polynomial) : ForceField {
    override fun forceAt(position: Vector2D, body: PhysicsBody): Vector2D {
        val speed = body.velocity.length()
        if (speed < 1e-9) return Vector2D.ZERO
        val magnitude = polynomial(speed)
        return -body.velocity / speed * magnitude
    }
}