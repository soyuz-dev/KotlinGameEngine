package org.soyuz.engine.physics

import org.soyuz.engine.collision.CollisionSystem
import org.soyuz.engine.scene.Scene
import org.soyuz.util.Vector2D

class RuntimePhysicsSystem (
    private val collisionSystem: CollisionSystem
) : PhysicsSystem  {

    private val bodies = mutableMapOf<String, PhysicsBody>()

    override fun registerBody(entityId: String, body: PhysicsBody) {
        bodies[entityId] = body
    }

    override fun unregisterBody(entityId: String) {
        bodies.remove(entityId)
    }
    override fun step(scene: Scene, dt: Double) {
        if (dt <= 0.0) return

        val displacements = mutableMapOf<String, Vector2D>()

        // Accumulate forces from force fields
        for ((entityId, body) in bodies) {
            val entity = scene.findEntity(entityId) ?: continue
            if (body is PointMass) {
                body.accumulateForces(entity.transform.position)
            }
        }

        // Phase 1: Position update
        for ((entityId, body) in bodies) {
            val entity = scene.findEntity(entityId) ?: continue
            val displacement = body.integratePosition(dt)
            displacements[entityId] = displacement
            entity.transform = entity.transform.translated(displacement)
        }

        // Phase 2: Collision detection & resolution

        val contacts = collisionSystem.detect(scene)

        for (contact in contacts) {
            val bodyA = bodies[contact.entityA]
            val bodyB = bodies[contact.entityB]

            val entityA = scene.findEntity(contact.entityA)?: continue
            val entityB = scene.findEntity(contact.entityB)?: continue
            if (bodyA == null || bodyB == null) continue

            val invMassA = if (bodyA is RigidBody) bodyA.inverseMass else if (bodyA is PointMass) bodyA.inverseMass else 0.0
            val invMassB = if (bodyB is RigidBody) bodyB.inverseMass else if (bodyB is PointMass) bodyB.inverseMass else 0.0

            if (invMassA == 0.0 && invMassB == 0.0) continue

            val relativeV = bodyB.velocity - bodyA.velocity
            val vAlongNormal = relativeV dot contact.normal

            if (vAlongNormal > 0) continue

            val e = minOf(
                bodyA.restitution,
                bodyB.restitution
            )

            val j = -(1.0+e) * vAlongNormal / (invMassA + invMassB)
            val impulse = contact.normal * j
            bodyA.applyImpulse(-impulse, contact.point - entityA.transform.position)
            bodyB.applyImpulse(impulse, contact.point - entityB.transform.position)

            // Positional correction
            val slop = 0.01
            val percent = 0.8
            val correctionMagnitude = maxOf(contact.depth - slop, 0.0) * percent / (invMassA + invMassB)
            val correction = contact.normal * correctionMagnitude


            entityA.transform = entityA.transform.translated(-correction * invMassA)
            entityB.transform = entityB.transform.translated(correction * invMassB)

        }



        // Phase 3: Velocity update
        for ((entityId, body) in bodies) {
            body.integrateVelocity(dt)
        }
    }
}