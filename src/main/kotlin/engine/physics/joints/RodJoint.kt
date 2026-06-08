package org.soyuz.engine.physics.joints

import org.soyuz.engine.physics.PhysicsBody
import org.soyuz.engine.physics.PointMass
import org.soyuz.engine.physics.RigidBody
import org.soyuz.util.Vector2D

class RodJoint(
    override val bodyA: PhysicsBody,
    override val bodyB: PhysicsBody,
    val restLength : Double
) : StrictJoint {

    private data class BodyEntry(val body: PhysicsBody, var position: Vector2D)

    override fun solvePositions(
        positionA: Vector2D,
        positionB: Vector2D,
        dt: Double
    ): Pair<Vector2D, Vector2D> {
        val delta = positionB - positionA
        val currentLength = delta.length()

        if (currentLength < Vector2D.EPSILON_NORMALIZE) return Pair(positionA, positionB)

        val invMassA = (bodyA as? PointMass)?.inverseMass ?: (bodyA as? RigidBody)?.inverseMass ?: 0.0
        val invMassB = (bodyB as? PointMass)?.inverseMass ?: (bodyB as? RigidBody)?.inverseMass ?: 0.0
        val totalInvMass = invMassA + invMassB
        if (totalInvMass < Vector2D.EPSILON_NORMALIZE) return Pair(positionA, positionB)

        val correction = (currentLength - restLength) / currentLength

        val correctionA = correction * invMassA / totalInvMass
        val correctionB = correction * invMassB / totalInvMass

        val newPosA = positionA + delta * correctionA
        val newPosB = positionB - delta * correctionB

        return Pair(newPosA, newPosB)
    }

}