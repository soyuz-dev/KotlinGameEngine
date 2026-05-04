package org.soyuz.engine.physics

import org.soyuz.util.Vector2D

interface PhysicsBody {

    var mass: Double

    var velocity: Vector2D

    fun applyForce(force: Vector2D)
}

