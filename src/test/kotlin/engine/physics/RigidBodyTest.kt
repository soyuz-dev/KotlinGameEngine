package org.soyuz.engine.physics

import org.soyuz.util.Vector2D
import kotlin.math.abs
import kotlin.test.*

class RigidBodyTest {

    private val identity = Vector2D(0.0, 0.0)

    // --- Construction ---

    @Test
    fun `default body has unit mass and zero velocity`() {
        val rb = RigidBody()
        assertEquals(1.0, rb.mass)
        assertEquals(Vector2D.ZERO, rb.velocity)
        assertEquals(0.0, rb.angularVelocity)
    }

    @Test
    fun `mass cannot be negative`() {
        assertFailsWith<IllegalArgumentException> {
            RigidBody(mass = -1.0)
        }
    }

    @Test
    fun `zero mass gives zero inverse inertia`() {
        val rb = RigidBody(mass = 0.0)
        assertEquals(0.0, rb.inverseInertia)
    }

    // --- Linear physics delegates to PointMass ---

    @Test
    fun `applyForce accumulates force`() {
        val rb = RigidBody(mass = 1.0)
        rb.applyForce(Vector2D(10.0, 0.0))
        val displacement = rb.integratePosition(1.0 / 60.0)
        assertTrue(displacement.x > 0.0)
    }

    @Test
    fun `linear impulse changes velocity`() {
        val rb = RigidBody(mass = 2.0)
        rb.applyImpulse(Vector2D(10.0, 0.0), identity)
        assertEquals(5.0, rb.velocity.x, 1e-9)
    }

    @Test
    fun `linear impulse does nothing for zero mass`() {
        val rb = RigidBody(mass = 0.0)
        rb.applyImpulse(Vector2D(10.0, 0.0), identity)
        assertEquals(Vector2D.ZERO, rb.velocity)
    }

    // --- Angular impulse ---

    @Test
    fun `impulse at offset produces angular velocity`() {
        val rb = RigidBody(mass = 1.0) // inverseInertia = 1.0
        val contactPoint = Vector2D(0.0, 2.0) // 2 units above center
        val impulse = Vector2D(5.0, 0.0) // horizontal hit

        // r = (0,2), impulse = (5,0), r × impulse = 0*0 - 2*5 = -10
        // Δω = -10 * 1.0 = -10
        rb.applyImpulse(impulse, contactPoint)
        assertEquals(-10.0, rb.angularVelocity, 1e-9)
        assertEquals(5.0, rb.velocity.x, 1e-9) // linear still applies
    }

    @Test
    fun `impulse at center produces no angular velocity`() {
        val rb = RigidBody(mass = 1.0)
        rb.applyImpulse(Vector2D(10.0, 0.0), identity) // hit at center
        assertEquals(0.0, rb.angularVelocity, 1e-9)
    }

    @Test
    fun `angular impulse respects inverse inertia`() {
        val rb = RigidBody(mass = 4.0) // inverseInertia = 0.25
        val contactPoint = Vector2D(1.0, 0.0)
        val impulse = Vector2D(0.0, 10.0) // r × impulse = 1*10 - 0*0 = 10

        rb.applyImpulse(impulse, contactPoint)
        assertEquals(2.5, rb.angularVelocity, 1e-9) // 10 * 0.25
    }

    @Test
    fun `angular impulse does nothing for zero mass`() {
        val rb = RigidBody(mass = 0.0)
        val contactPoint = Vector2D(0.0, 5.0)
        val impulse = Vector2D(10.0, 0.0)

        rb.applyImpulse(impulse, contactPoint)
        assertEquals(0.0, rb.angularVelocity, 1e-9)
    }

    // --- Torque ---

    @Test
    fun `torque affects angular velocity during integration`() {
        val rb = RigidBody(mass = 1.0)
        rb.applyTorque(5.0)

        rb.integratePosition(1.0 / 60.0)
        rb.integrateVelocity(1.0 / 60.0)

        // Δω = torque * invInertia * dt = 5 * 1 * 1/60 ≈ 0.0833
        assertEquals(5.0 / 60.0, rb.angularVelocity, 1e-9)
    }

    @Test
    fun `torque is cleared after integration`() {
        val rb = RigidBody(mass = 1.0)
        rb.applyTorque(10.0)

        rb.integratePosition(1.0 / 60.0)
        rb.integrateVelocity(1.0 / 60.0)

        val w1 = rb.angularVelocity

        // Second step with no new torque
        rb.integratePosition(1.0 / 60.0)
        rb.integrateVelocity(1.0 / 60.0)

        // Angular velocity should remain constant (no new torque)
        assertEquals(w1, rb.angularVelocity, 1e-9)
    }

    // --- Integration ---

    @Test
    fun `integratePosition returns linear displacement`() {
        val rb = RigidBody(mass = 1.0)
        rb.velocity = Vector2D(10.0, 0.0)

        val dt = 1.0 / 60.0
        val displacement = rb.integratePosition(dt)
        assertEquals(10.0 * dt, displacement.x, 1e-9)
    }

    @Test
    fun `integrateVelocity updates both linear and angular`() {
        val rb = RigidBody(mass = 1.0)
        rb.velocity = Vector2D(5.0, 0.0)
        rb.angularVelocity = 2.0
        rb.applyForce(Vector2D(10.0, 0.0)) // a0 = 10

        val dt = 1.0 / 60.0
        rb.integratePosition(dt)
        rb.integrateVelocity(dt, Vector2D(10.0, 0.0)) // constant force

        // v += 0.5*(10+10)*dt = 10*dt
        assertEquals(5.0 + 10.0 * dt, rb.velocity.x, 1e-9)
        // angular unchanged (no torque)
        assertEquals(2.0, rb.angularVelocity, 1e-9)
    }

    // --- Restitution and friction ---

    @Test
    fun `restitution defaults to 0_3`() {
        val rb = RigidBody()
        assertEquals(0.3, rb.restitution, 1e-9)
    }

    @Test
    fun `friction defaults to 0_2`() {
        val rb = RigidBody()
        assertEquals(0.2, rb.friction, 1e-9)
    }

    // --- mass property delegates to PointMass ---

    @Test
    fun `changing mass updates inverse inertia`() {
        val rb = RigidBody(mass = 2.0)
        assertEquals(0.5, rb.inverseInertia, 1e-9)

        rb.mass = 4.0
        assertEquals(0.25, rb.inverseInertia, 1e-9)
    }
}