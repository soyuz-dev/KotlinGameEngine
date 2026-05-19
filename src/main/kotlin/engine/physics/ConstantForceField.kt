package org.soyuz.engine.physics

import org.soyuz.util.Vector2D


class ConstantForceField(private val force: Vector2D) : ForceField {
    override fun forceAt(position: Vector2D, velocity: Vector2D) = force
}