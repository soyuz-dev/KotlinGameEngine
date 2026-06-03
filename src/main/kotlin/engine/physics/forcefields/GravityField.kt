package org.soyuz.engine.physics.forcefields

import org.soyuz.engine.physics.PhysicsBody
import org.soyuz.util.Vector2D
import kotlin.collections.iterator

class GravityField(private val G: Double = 5e6) : DynamicForceField {
    private data class BodyEntry(val body: PhysicsBody, var position: Vector2D)
    private val bodies = mutableMapOf<String, BodyEntry>()


    override fun registerBody(entityId: String, body: PhysicsBody, position: Vector2D) {
        bodies[entityId] = BodyEntry(body, position)
        body.addField(this)
    }

    override fun unregisterBody(entityId: String) {
        bodies.remove(entityId)
    }

    override fun updatePosition(entityId: String, position: Vector2D) {
        bodies[entityId]?.position = position
    }

    override fun forceAt(position: Vector2D, body: PhysicsBody): Vector2D {
        var totalForce = Vector2D.ZERO
        for ((_, other) in bodies) {
            if (other.body === body) continue
            val delta = other.position - position
            val distSq = delta.lengthSquared()
            if (distSq < 1.0) continue // prevent singularity
            val direction = delta / kotlin.math.sqrt(distSq) // normalized
            val magnitude = G * other.body.mass * body.mass / distSq
            totalForce += direction * magnitude
        }
        return totalForce
    }
}