package org.soyuz.engine.physics.forcefields

import org.soyuz.engine.physics.PhysicsBody
import org.soyuz.util.math.Vector2D

class ConstantAccelerationField(private val acceleration: Vector2D) : ForceField {
    override fun forceAt(position: Vector2D, body: PhysicsBody): Vector2D {
        return acceleration * body.mass
    }
}