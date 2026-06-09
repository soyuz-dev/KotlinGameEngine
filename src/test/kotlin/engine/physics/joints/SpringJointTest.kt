package org.soyuz.engine.physics.joints

import org.soyuz.engine.physics.PointMass
import org.soyuz.util.Vector2D
import kotlin.test.*

class SpringJointTest {

    @Test
    fun `spring applies force to pull bodies toward rest length`() {
        val a = PointMass(mass = 1.0)
        val b = PointMass(mass = 1.0)
        val joint = SpringJoint(a, b, restLength = 10.0, stiffness = 100.0)

        // Bodies stretched to distance 20
        joint.accumulateForces(Vector2D(0.0, 0.0), Vector2D(20.0, 0.0))

        // Force on A should be positive x (pulled toward B)
        // Force magnitude = stiffness * (20 - 10) = 1000
        // After integration: a = F/m = 1000, displacement > 0
        val dispA = a.integratePosition(1.0 / 60.0)
        assertTrue(dispA.x > 0.0)
    }

    @Test
    fun `spring applies force to push bodies apart when compressed`() {
        val a = PointMass(mass = 1.0)
        val b = PointMass(mass = 1.0)
        val joint = SpringJoint(a, b, restLength = 10.0, stiffness = 100.0)

        // Bodies compressed to distance 2
        joint.accumulateForces(Vector2D(0.0, 0.0), Vector2D(2.0, 0.0))

        // Force on A should be negative x (pushed away from B)
        val dispA = a.integratePosition(1.0 / 60.0)
        assertTrue(dispA.x < 0.0)
    }

    @Test
    fun `spring applies no force at rest length`() {
        val a = PointMass(mass = 1.0)
        val b = PointMass(mass = 1.0)
        val joint = SpringJoint(a, b, restLength = 10.0, stiffness = 100.0)

        joint.accumulateForces(Vector2D(0.0, 0.0), Vector2D(10.0, 0.0))

        val dispA = a.integratePosition(1.0 / 60.0)
        assertEquals(Vector2D.ZERO, dispA)
    }

    @Test
    fun `spring force scales with stiffness`() {
        val a1 = PointMass(mass = 1.0)
        val b1 = PointMass(mass = 1.0)
        val stiff = SpringJoint(a1, b1, restLength = 10.0, stiffness = 100.0)

        val a2 = PointMass(mass = 1.0)
        val b2 = PointMass(mass = 1.0)
        val weak = SpringJoint(a2, b2, restLength = 10.0, stiffness = 10.0)

        stiff.accumulateForces(Vector2D(0.0, 0.0), Vector2D(20.0, 0.0))
        weak.accumulateForces(Vector2D(0.0, 0.0), Vector2D(20.0, 0.0))

        val dispStiff = a1.integratePosition(1.0 / 60.0)
        val dispWeak = a2.integratePosition(1.0 / 60.0)

        assertTrue(dispStiff.x > dispWeak.x, "Stiffer spring should produce more force")
    }

    @Test
    fun `damping reduces relative velocity`() {
        val a = PointMass(mass = 1.0)
        val b = PointMass(mass = 1.0)
        b.velocity = Vector2D(100.0, 0.0) // moving away fast
        val joint = SpringJoint(a, b, restLength = 10.0, stiffness = 0.0, damping = 10.0)

        joint.accumulateForces(Vector2D(0.0, 0.0), Vector2D(10.0, 0.0))

        val dispA = a.integratePosition(1.0 / 60.0)
        // Damping should pull A toward B to resist the velocity difference
        assertTrue(dispA.x > 0.0)
    }

    @Test
    fun `spring does nothing when bodies at same position`() {
        val a = PointMass(mass = 1.0)
        val b = PointMass(mass = 1.0)
        val joint = SpringJoint(a, b, restLength = 10.0, stiffness = 100.0)

        val pos = Vector2D(5.0, 5.0)
        joint.accumulateForces(pos, pos)

        val dispA = a.integratePosition(1.0 / 60.0)
        assertEquals(Vector2D.ZERO, dispA)
    }

    @Test
    fun `spring works with zero mass bodies`() {
        val a = PointMass(mass = 0.0)
        val b = PointMass(mass = 1.0)
        val joint = SpringJoint(a, b, restLength = 10.0, stiffness = 100.0)

        // Should not crash
        joint.accumulateForces(Vector2D(0.0, 0.0), Vector2D(20.0, 0.0))

        val dispA = a.integratePosition(1.0 / 60.0)
        assertEquals(Vector2D.ZERO, dispA) // infinite mass doesn't move
    }
}