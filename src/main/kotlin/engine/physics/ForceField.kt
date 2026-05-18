package org.soyuz.engine.physics

import org.soyuz.engine.entity.GameEntity
import org.soyuz.util.Vector2D

interface ForceField {
    fun forceAt(position: Vector2D, velocity: Vector2D) : Vector2D
}

class ConstantForceField(private val force: Vector2D) : ForceField {
    override fun forceAt(position: Vector2D, velocity: Vector2D) = force
}