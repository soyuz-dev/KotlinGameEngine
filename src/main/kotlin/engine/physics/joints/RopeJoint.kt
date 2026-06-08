package org.soyuz.engine.physics.joints

import org.soyuz.engine.physics.PhysicsBody
import org.soyuz.util.Vector2D

class RopeJoint(
    override val bodyA: PhysicsBody,
    override val bodyB: PhysicsBody,
    val restLength: Double,
    val stiffness: Double,
    val damping: Double = 0.0
) : PermissiveJoint {
    override fun accumulateForces(positionA: Vector2D, positionB: Vector2D) {
        val delta = positionB - positionA
        val currentLength = delta.length()
        if(currentLength < Vector2D.EPSILON_NORMALIZE) return
        val direction = delta/currentLength

        val springForce = direction * (stiffness * (currentLength-restLength))
        bodyA.applyForce(springForce)
        bodyB.applyForce(-springForce)

        val deltaV = bodyB.velocity - bodyA.velocity
        val dampingForce = direction * (damping * (deltaV dot direction))
        bodyA.applyForce(dampingForce)
        bodyB.applyForce(-dampingForce)
    }
}