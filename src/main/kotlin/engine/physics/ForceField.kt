package org.soyuz.engine.physics

import org.soyuz.util.Vector2D

interface ForceField {
    fun forceAt(position: Vector2D, velocity: Vector2D) : Vector2D
}
