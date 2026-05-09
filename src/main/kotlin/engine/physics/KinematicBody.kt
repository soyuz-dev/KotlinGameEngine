package org.soyuz.engine.physics

import org.soyuz.util.Vector2D

class KinematicBody(override var velocity: Vector2D) : PhysicsBody {
    override var mass: Double = 0.0
        set(_) = Unit
    override fun applyForce(force: Vector2D) {
    }

    override fun integratePosition(dt: Double): Vector2D {
        if (dt <= 0.0) return Vector2D.ZERO
        return velocity * dt
    }
}