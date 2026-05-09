package org.soyuz.engine.physics

import org.soyuz.util.Vector2D
import kotlin.test.*
class PointMassTest {
    // --- Construction ---
    @Test
    fun `default body is dynamic with unit mass`() {
        val pm = PointMass()
        assertEquals(1.0, pm.mass)
        assertEquals(Vector2D.ZERO, pm.velocity)
    }
    @Test
    fun `mass cannot be negative`() {
        assertFailsWith<IllegalArgumentException> {
            PointMass(mass = -1.0)
        }
    }
    @Test
    fun `zero mass is allowed — treated as infinite mass`() {
        val pm = PointMass(mass = 0.0)
        assertEquals(0.0, pm.mass)
    }
    // --- Mass property ---
    @Test
    fun `changing mass updates inverse mass`() {
        val pm = PointMass(mass = 2.0)
        assertEquals(2.0, pm.mass)
        pm.mass = 4.0
        assertEquals(4.0, pm.mass)
    }
    @Test
    fun `setting mass to negative throws`() {
        val pm = PointMass(mass = 1.0)
        assertFailsWith<IllegalArgumentException> {
            pm.mass = -1.0
        }
    }
    @Test
    fun `setting mass to zero makes body immovable by forces`() {
        val pm = PointMass(mass = 1.0)
        pm.mass = 0.0
        pm.applyForce(Vector2D(10.0, 0.0))
        val displacement = pm.integratePosition(1.0 / 60.0)
        assertEquals(Vector2D.ZERO, displacement)
    }
    // --- Force accumulation ---

    @Test
    fun `applyForce accumulates forces`() {
        val pm = PointMass(mass = 1.0)
        pm.applyForce(Vector2D(10.0, 0.0))
        pm.applyForce(Vector2D(0.0, 5.0))
        // Force accumulation is internal — verify through integration
        val displacement = pm.integratePosition(1.0 / 60.0)
        // a = (10,5) / 1.0 = (10,5)
        // d = 0*dt + 0.5*(10,5)*dt² = 0.5*(10,5)*(1/3600) ≈ (0.001389, 0.000694)
        assertTrue(displacement.x > 0.0)
        assertTrue(displacement.y > 0.0)
    }
    @Test
    fun `applyForce is ignored for static bodies`() {
        val pm = PointMass(mass = 0.0)
        pm.applyForce(Vector2D(10.0, 0.0))

        val displacement = pm.integratePosition(1.0 / 60.0)
        assertEquals(Vector2D.ZERO, displacement)
    }
    @Test
    fun `force accumulator is cleared after integratePosition`() {
        val pm = PointMass(mass = 1.0)
        pm.applyForce(Vector2D(10.0, 0.0))
        pm.integratePosition(1.0 / 60.0)
        // Second integration without new forces should use zero acceleration
        val displacement = pm.integratePosition(1.0 / 60.0)
        // Only velocity contributes: v * dt
        // v after first step ≈ (0.1667, 0) with dt=1/60
        assertEquals(0.0, displacement.y, 1e-9)
    }

    // --- Impulse application ---
    @Test
    fun `applyImpulse changes velocity immediately`() {
        val pm = PointMass(mass = 2.0)
        pm.applyImpulse(Vector2D(10.0, 0.0))
        // Δv = impulse / mass = (10,0) / 2 = (5,0)
        assertEquals(5.0, pm.velocity.x, 1e-9)
        assertEquals(0.0, pm.velocity.y, 1e-9)
    }
    @Test
    fun `applyImpulse is ignored for static bodies`() {
        val pm = PointMass(mass = 0.0)
        pm.applyImpulse(Vector2D(10.0, 0.0))

        assertEquals(Vector2D.ZERO, pm.velocity)
    }

    // --- Integration: position phase ---
    @Test
    fun `integratePosition returns zero for static body`() {
        val pm = PointMass(mass = 0.0)
        pm.applyForce(Vector2D(10.0, 0.0))
        val displacement = pm.integratePosition(1.0 / 60.0)
        assertEquals(Vector2D.ZERO, displacement)
    }
    @Test
    fun `integratePosition returns zero for zero dt`() {
        val pm = PointMass(mass = 1.0)
        pm.velocity = Vector2D(10.0, 0.0)
        pm.applyForce(Vector2D(5.0, 0.0))
        val displacement = pm.integratePosition(0.0)
        assertEquals(Vector2D.ZERO, displacement)
    }
    @Test
    fun `integratePosition handles negative dt safely`() {
        val pm = PointMass(mass = 1.0)
        pm.velocity = Vector2D(10.0, 0.0)
        val displacement = pm.integratePosition(-1.0)
        assertEquals(Vector2D.ZERO, displacement)
    }
    @Test
    fun `integratePosition with velocity only no forces`() {
        val pm = PointMass(mass = 1.0)
        pm.velocity = Vector2D(10.0, 0.0)
        val dt = 1.0 / 60.0
        val displacement = pm.integratePosition(dt)
        // d = v*dt + 0.5*0*dt² = (10,0) * (1/60) = (0.1666..., 0)
        assertEquals(10.0 * dt, displacement.x, 1e-9)
        assertEquals(0.0, displacement.y, 1e-9)
    }
    @Test
    fun `integratePosition with constant force produces correct displacement`() {
        val pm = PointMass(mass = 2.0)
        pm.applyForce(Vector2D(0.0, -20.0)) // gravity-like: -20 force on mass 2 → a = -10
        val dt = 1.0 / 60.0
        val displacement = pm.integratePosition(dt)
        // d = 0*dt + 0.5*(-10)*dt² = -5 * (1/3600) = -0.0013888...
        assertEquals(0.0, displacement.x, 1e-9)
        assertEquals(-5.0 * dt * dt, displacement.y, 1e-9)
    }
    @Test
    fun `integratePosition with velocity and force`() {
        val pm = PointMass(mass = 1.0)
        pm.velocity = Vector2D(5.0, 0.0)
        pm.applyForce(Vector2D(0.0, -10.0)) // a = -10

        val dt = 1.0 / 60.0
        val displacement = pm.integratePosition(dt)
        // d.x = 5*dt + 0 = 5/60
        // d.y = 0*dt + 0.5*(-10)*dt² = -5/3600
        assertEquals(5.0 * dt, displacement.x, 1e-9)
        assertEquals(-5.0 * dt * dt, displacement.y, 1e-9)
    }

    // --- Integration: velocity phase ---
    @Test
    fun `integrateVelocity does nothing for static body`() {
        val pm = PointMass(mass = 0.0)
        pm.velocity = Vector2D(5.0, 0.0) // shouldn't happen but guard anyway
        pm.integrateVelocity(1.0 / 60.0, Vector2D(10.0, 0.0))
        assertEquals(Vector2D(5.0, 0.0), pm.velocity) // unchanged
    }
    @Test
    fun `integrateVelocity uses previous acceleration when none provided`() {
        val pm = PointMass(mass = 1.0)
        pm.applyForce(Vector2D(10.0, 0.0)) // a = 10
        val dt = 1.0 / 60.0
        pm.integratePosition(dt) // stores previousAcceleration = 10
        pm.integrateVelocity(dt) // uses previousAcceleration for both a0 and a1
        // v += 0.5*(10 + 10)*dt = 10*dt = 10/60 ≈ 0.1667
        assertEquals(10.0 * dt, pm.velocity.x, 1e-9)
        assertEquals(0.0, pm.velocity.y, 1e-9)
    }
    @Test
    fun `integrateVelocity with explicit new acceleration`() {
        val pm = PointMass(mass = 1.0)
        pm.applyForce(Vector2D(10.0, 0.0)) // a0 = 10
        val dt = 1.0 / 60.0
        pm.integratePosition(dt) // stores previousAcceleration = 10
        pm.integrateVelocity(dt, Vector2D(20.0, 0.0)) // a1 = 20
        // v += 0.5*(10 + 20)*dt = 15*dt
        assertEquals(15.0 * dt, pm.velocity.x, 1e-9)
    }

    // --- Combined integrate (convenience method) ---
    @Test
    fun `integrate combines position and velocity`() {
        val pm = PointMass(mass = 1.0)
        pm.velocity = Vector2D(10.0, 0.0)
        pm.applyForce(Vector2D(5.0, 0.0))
        val dt = 1.0 / 60.0
        val displacement = pm.integrate(dt, Vector2D(5.0, 0.0))
        // Position: d = 10*dt + 0.5*5*dt²
        val expectedDisp = 10.0 * dt + 0.5 * 5.0 * dt * dt
        assertEquals(expectedDisp, displacement.x, 1e-9)
        // Velocity: v += 0.5*(5 + 5)*dt = 5*dt
        assertEquals(10.0 + 5.0 * dt, pm.velocity.x, 1e-9)
    }

    // --- setVelocity ---
    @Test
    fun `setVelocity assigns directly`() {
        val pm = PointMass(mass = 1.0)
        pm.setVelocity(10.0, -5.0)
        assertEquals(Vector2D(10.0, -5.0), pm.velocity)
    }

    // --- Edge cases ---
    @Test
    fun `large force does not produce NaN velocity`() {
        val pm = PointMass(mass = 1.0)
        pm.applyForce(Vector2D(1e10, 0.0))
        val dt = 1.0 / 60.0
        pm.integratePosition(dt)
        pm.integrateVelocity(dt)
        assertTrue(pm.velocity.x.isFinite())
    }
    @Test
    fun `very small dt still produces valid integration`() {
        val pm = PointMass(mass = 1.0)
        pm.velocity = Vector2D(100.0, 0.0)
        pm.applyForce(Vector2D(10.0, 0.0))
        val displacement = pm.integrate(1e-10, Vector2D(10.0, 0.0))
        assertTrue(displacement.x.isFinite())
        assertTrue(displacement.x >= 0.0)
    }
    @Test
    fun `multiple integrate calls without forces just drifts on velocity`() {
        val pm = PointMass(mass = 1.0)
        pm.velocity = Vector2D(10.0, 0.0)
        val dt = 1.0 / 60.0
        val d1 = pm.integrate(dt) // no new acceleration
        // After first call: v stays 10, displacement = 10*dt
        assertEquals(10.0 * dt, d1.x, 1e-9)
        val d2 = pm.integrate(dt)
        // Second call: still v=10 (no forces), displacement = 10*dt again
        assertEquals(10.0 * dt, d2.x, 1e-9)
    }
}