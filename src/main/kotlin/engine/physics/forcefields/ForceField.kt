package org.soyuz.engine.physics.forcefields

import org.soyuz.engine.physics.PhysicsBody
import org.soyuz.util.math.Vector2D

interface ForceField {
    fun forceAt(position: Vector2D, body: PhysicsBody) : Vector2D
}