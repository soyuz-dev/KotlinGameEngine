package org.soyuz.engine.physics.joints

import org.soyuz.engine.physics.PointMass
import org.soyuz.util.math.Vector2D
import kotlin.test.*

class RodJointTest {

    @Test
    fun `rod pulls bodies together when stretched`() {
        val a = PointMass(mass = 1.0)
        val b = PointMass(mass = 1.0)
        val joint = RodJoint(a, b, restLength = 10.0)

        val (newA, newB) = joint.solvePositions(
            Vector2D(0.0, 0.0),
            Vector2D(20.0, 0.0), // distance = 20, should shrink to 10
            1.0 / 60.0
        )

        val newDist = (newB - newA).length()
        assertEquals(10.0, newDist, 1e-9)
    }

    @Test
    fun `rod pushes bodies apart when compressed`() {
        val a = PointMass(mass = 1.0)
        val b = PointMass(mass = 1.0)
        val joint = RodJoint(a, b, restLength = 10.0)

        val (newA, newB) = joint.solvePositions(
            Vector2D(0.0, 0.0),
            Vector2D(2.0, 0.0), // distance = 2, should expand to 10
            1.0 / 60.0
        )

        val newDist = (newB - newA).length()
        assertEquals(10.0, newDist, 1e-9)
    }

    @Test
    fun `rod does nothing when at rest length`() {
        val a = PointMass(mass = 1.0)
        val b = PointMass(mass = 1.0)
        val joint = RodJoint(a, b, restLength = 10.0)

        val posA = Vector2D(0.0, 0.0)
        val posB = Vector2D(10.0, 0.0)
        val (newA, newB) = joint.solvePositions(posA, posB, 1.0 / 60.0)

        assertEquals(posA, newA)
        assertEquals(posB, newB)
    }

    @Test
    fun `rod handles zero mass body as anchor`() {
        val a = PointMass(mass = 0.0) // static anchor
        val b = PointMass(mass = 1.0)
        val joint = RodJoint(a, b, restLength = 10.0)

        val anchorPos = Vector2D(0.0, 0.0)
        val (newA, newB) = joint.solvePositions(
            anchorPos,
            Vector2D(20.0, 0.0), // stretched to 20
            1.0 / 60.0
        )

        assertEquals(anchorPos, newA) // anchor doesn't move
        val dist = (newB - newA).length()
        assertEquals(10.0, dist, 1e-9)
    }

    @Test
    fun `rod handles zero mass for both bodies`() {
        val a = PointMass(mass = 0.0)
        val b = PointMass(mass = 0.0)
        val joint = RodJoint(a, b, restLength = 10.0)

        val posA = Vector2D(0.0, 0.0)
        val posB = Vector2D(20.0, 0.0)
        val (newA, newB) = joint.solvePositions(posA, posB, 1.0 / 60.0)

        assertEquals(posA, newA) // neither moves
        assertEquals(posB, newB)
    }

    @Test
    fun `rod handles bodies at same position`() {
        val a = PointMass(mass = 1.0)
        val b = PointMass(mass = 1.0)
        val joint = RodJoint(a, b, restLength = 10.0)

        val pos = Vector2D(5.0, 5.0)
        val (newA, newB) = joint.solvePositions(pos, pos, 1.0 / 60.0)

        // Should not crash, just return unchanged
        assertEquals(pos, newA)
        assertEquals(pos, newB)
    }

    @Test
    fun `rod moves heavier body less`() {
        val a = PointMass(mass = 1.0)
        val b = PointMass(mass = 3.0) // 3x heavier
        val joint = RodJoint(a, b, restLength = 10.0)

        val posA = Vector2D(0.0, 0.0)
        val posB = Vector2D(20.0, 0.0)
        val (newA, newB) = joint.solvePositions(posA, posB, 1.0 / 60.0)

        val dist = (newB - newA).length()
        assertEquals(10.0, dist, 1e-9)

        val moveA = (newA - posA).length()
        val moveB = (newB - posB).length()
        assertTrue(moveA > moveB, "Lighter body A should move more than heavier body B")
    }
}