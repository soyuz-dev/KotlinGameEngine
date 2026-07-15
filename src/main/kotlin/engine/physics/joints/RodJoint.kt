package org.soyuz.engine.physics.joints

import org.soyuz.engine.physics.PhysicsBody
import org.soyuz.engine.physics.PointMass
import org.soyuz.engine.physics.RigidBody
import org.soyuz.util.math.Vector2D


class RodJoint(
    override val bodyA: PhysicsBody,
    override val bodyB: PhysicsBody,
    val restLength: Double
) : StrictJoint {

    private val beta = 0.3  // Stabilization factor (tune between 0.0-0.2)

    override fun solvePositions(
        positionA: Vector2D,
        positionB: Vector2D,
        dt: Double
    ): Pair<Vector2D, Vector2D> {
        val delta = positionB - positionA
        val currentLength = delta.length()

        if (currentLength < Vector2D.EPSILON_NORMALIZE) return Pair(positionA, positionB)

        val invMassA = getInverseMass(bodyA)
        val invMassB = getInverseMass(bodyB)
        val totalInvMass = invMassA + invMassB

        if (totalInvMass < Vector2D.EPSILON_NORMALIZE) return Pair(positionA, positionB)

        // --- Position Correction ---
        val correction = (currentLength - restLength) / currentLength
        val correctionA = correction * invMassA / totalInvMass
        val correctionB = correction * invMassB / totalInvMass

        val newPosA = positionA + delta * correctionA
        val newPosB = positionB - delta * correctionB

        // --- Baumgarte Velocity Stabilization ---
        val direction = delta.normalized()
        val relVel = (bodyB.velocity - bodyA.velocity).dot(direction)
        val positionError = currentLength - restLength

        // Target relative velocity: bias toward reducing the error
        val targetRelVel = -beta * positionError / maxOf(dt, 1e-4)
        val velCorrectionNeeded = targetRelVel - relVel

        val impulseScale = velCorrectionNeeded / totalInvMass

        if (bodyA is PointMass) {
            bodyA.velocity = bodyA.velocity - (direction * impulseScale * invMassA)
        } else if (bodyA is RigidBody) {
            bodyA.velocity = bodyA.velocity - (direction * impulseScale * invMassA)
        }

        if (bodyB is PointMass) {
            bodyB.velocity = bodyB.velocity + (direction * impulseScale * invMassB)
        } else if (bodyB is RigidBody) {
            bodyB.velocity = bodyB.velocity + (direction * impulseScale * invMassB)
        }

        return Pair(newPosA, newPosB)
    }

    private fun getInverseMass(body: PhysicsBody): Double = when (body) {
        is PointMass -> body.inverseMass
        is RigidBody -> body.inverseMass
        else -> 0.0
    }
}