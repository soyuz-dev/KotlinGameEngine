package org.soyuz.engine.physics


import org.soyuz.util.Vector2D

class PointMass(
    mass: Double = 1.0,
    override var isStatic: Boolean = false
) : PhysicsBody {

    private var _mass: Double = mass
        set(value) {
            field = value
            inverseMass = if (value > 0.0) 1.0 / value else 0.0
        }

    private var inverseMass: Double = if (mass > 0.0) 1.0 / mass else 0.0
        private set

    override var mass: Float
        get() = _mass.toFloat()
        set(value) { _mass = value.toDouble() }

    override var velocity: Vector2D = Vector2D.ZERO

    // Accumulated force for the *current* frame
    private var forceAccum: Vector2D = Vector2D.ZERO

    // Previous frame's acceleration (for Verlet)
    private var previousAcceleration: Vector2D = Vector2D.ZERO

    override fun applyForce(force: Vector2D) {
        if (isStatic) return
        forceAccum += force
    }

    fun applyImpulse(impulse: Vector2D) {
        if (isStatic) return
        velocity += impulse * inverseMass
    }

    override fun setVelocity(x: Float, y: Float) {
        velocity = Vector2D(x.toDouble(), y.toDouble())
    }

    /**
     * Velocity Verlet integration.
     * Returns the displacement to apply to the entity's Transform.
     *
     * Algorithm:
     *   a(t) = forceAccum * invMass
     *   x(t+dt) = x(t) + v(t)*dt + 0.5*a(t)*dt²
     *   (resolve collisions here, which may modify velocity via impulses)
     *   a(t+dt) = compute from forces at new position
     *   v(t+dt) = v(t) + 0.5*(a(t) + a(t+dt))*dt
     *
     * Since this class doesn't own position, we split the integration
     * into two phases that the PhysicsSystem orchestrates.
     */

    // Phase 1: Compute acceleration from accumulated forces,
    //          then return displacement for position update
    fun integratePosition(dt: Double): Vector2D {
        if (isStatic) {
            forceAccum = Vector2D.ZERO
            return Vector2D.ZERO
        }

        val currentAcceleration = forceAccum * inverseMass
        forceAccum = Vector2D.ZERO // Clear for next frame

        // x(t+dt) = x(t) + v(t)*dt + 0.5*a(t)*dt²
        val displacement = velocity * dt + currentAcceleration * (0.5 * dt * dt)

        // Store current acceleration for velocity update phase
        previousAcceleration = currentAcceleration

        return displacement
    }

    // Phase 2: Update velocity using average of previous and new acceleration.
    // Call this AFTER position is updated and any collision impulses are resolved.
    fun integrateVelocity(dt: Double, newAcceleration: Vector2D? = null) {
        if (isStatic) return

        // If no new acceleration provided (e.g., no collision-derived forces),
        // assume it's the same as previous (constant force approximation)
        val aNext = newAcceleration ?: previousAcceleration

        // v(t+dt) = v(t) + 0.5*(a(t) + a(t+dt))*dt
        velocity += (previousAcceleration + aNext) * (0.5 * dt)
    }

    // Convenience: single-call Verlet when you know new acceleration
    // (Useful when the physics system recomputes forces after position update)
    fun integrate(dt: Double, newAcceleration: Vector2D? = null): Vector2D {
        val displacement = integratePosition(dt)
        integrateVelocity(dt, newAcceleration)
        return displacement
    }
}