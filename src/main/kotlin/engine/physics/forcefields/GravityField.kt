package org.soyuz.engine.physics.forcefields

import org.soyuz.engine.physics.PhysicsBody
import org.soyuz.util.math.Vector2D
import kotlin.collections.iterator

class GravityField(private val G: Double = 5e6) : EntityAwareForceField {
    private data class BodyEntry(val body: PhysicsBody, var position: Vector2D)
    private val bodies = mutableMapOf<String, BodyEntry>()

    private val THRESHOLD = run {
        val minForce = 0.01
        ((minForce.toRawBits() shr 52) and 0x7FF) - 1023 // unbiased
    }

    private fun forceExp(mass: Double, otherMass: Double, distSq: Double): Long {
        val expG = ((G.toRawBits() shr 52) and 0x7FF) - 1023
        val expM1 = ((mass.toRawBits() shr 52) and 0x7FF) - 1023
        val expM2 = ((otherMass.toRawBits() shr 52) and 0x7FF) - 1023
        val expR2 = ((distSq.toRawBits() shr 52) and 0x7FF) - 1023
        return expG + expM1 + expM2 - expR2
    }

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
            if (forceExp(body.mass, other.body.mass, distSq) < THRESHOLD) continue
            val direction = delta / kotlin.math.sqrt(distSq) // normalized
            val magnitude = G * other.body.mass * body.mass / distSq
            totalForce += direction * magnitude
        }
        return totalForce
    }
}