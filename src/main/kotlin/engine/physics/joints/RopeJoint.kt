package org.soyuz.engine.physics.joints

import org.soyuz.engine.physics.PhysicsBody
import org.soyuz.engine.physics.PointMass
import org.soyuz.engine.physics.RigidBody
import org.soyuz.util.Vector2D


class RopeJoint(
    override val bodyA: PhysicsBody,
    override val bodyB: PhysicsBody,
    val maxLength: Double
) : StrictJoint {

    private val beta = 0.1  // lower than rod since ropes are less stiff

    override fun solvePositions(
        positionA: Vector2D,
        positionB: Vector2D,
        dt: Double
    ): Pair<Vector2D, Vector2D> {
        val delta = positionB - positionA
        val currentLength = delta.length()

        if (currentLength <= maxLength) return Pair(positionA, positionB)
        if (currentLength < Vector2D.EPSILON_NORMALIZE) return Pair(positionA, positionB)

        val invMassA = getInverseMass(bodyA)
        val invMassB = getInverseMass(bodyB)
        val totalInvMass = invMassA + invMassB
        if (totalInvMass < Vector2D.EPSILON_NORMALIZE) return Pair(positionA, positionB)

        // Position correction
        val correction = (currentLength - maxLength) / currentLength
        val correctionA = correction * invMassA / totalInvMass
        val correctionB = correction * invMassB / totalInvMass

        val newPosA = positionA + delta * correctionA
        val newPosB = positionB - delta * correctionB

        // Baumgarte velocity stabilization
        val direction = delta.normalized()
        val relVel = (bodyB.velocity - bodyA.velocity).dot(direction)
        val positionError = currentLength - maxLength
        val targetRelVel = -beta * positionError / maxOf(dt, 1e-4)
        val velCorrectionNeeded = targetRelVel - relVel
        val impulseScale = velCorrectionNeeded / totalInvMass

        bodyA.velocity -= direction * impulseScale * invMassA
        bodyB.velocity += direction * impulseScale * invMassB

        return Pair(newPosA, newPosB)
    }

    private fun getInverseMass(body: PhysicsBody): Double = when (body) {
        is PointMass -> body.inverseMass
        is RigidBody -> body.inverseMass
        else -> 0.0
    }
}