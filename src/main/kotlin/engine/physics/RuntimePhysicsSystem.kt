package org.soyuz.engine.physics

import org.soyuz.engine.scene.Scene
import org.soyuz.util.Vector2D

class RuntimePhysicsSystem : PhysicsSystem {

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

        // Phase 2: Collision detection & resolution TODO

        // Phase 3: Velocity update
        for ((entityId, body) in bodies) {
            body.integrateVelocity(dt)
        }
    }
}