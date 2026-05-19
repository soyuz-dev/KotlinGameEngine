package org.soyuz.engine.collision

import org.soyuz.engine.scene.Scene
import org.soyuz.util.Debug
import org.soyuz.util.MathUtil
import org.soyuz.util.Transform
import org.soyuz.util.Vector2D

class RuntimeCollisionSystem : CollisionSystem {

    private val colliders = mutableMapOf<String, Collider>()

    override fun registerCollider(entityId: String, collider: Collider) {
        require(entityId !in colliders) {"Entity '${entityId}' already has a registered collider."}
        require(colliders.values.none {it === collider}) {"Collider already registered to another entity. Create a new collider instead."}
        colliders[entityId] = collider
    }

    override fun unregisterCollider(entityId: String) {
        colliders.remove(entityId)
    }

    override fun detect(scene: Scene): MutableList<Contact> {
        val entities = scene.allEntities().filter { it.id in colliders }
        val contacts = mutableListOf<Contact>()

        for (i in entities.indices) {
            for (j in i+1 until entities.size) {
                val a = entities[i]
                val b = entities[j]
                val colliderA = colliders[a.id]!!
                val colliderB = colliders[b.id]!!

                if (colliderA.intersects(colliderB, a.transform, b.transform)) {
                    val contact = computeContact(a.id, b.id, colliderA, colliderB, a.transform, b.transform)
                    if (contact != null) {
                        contacts.add(contact)
                        Debug.log {"Contact: $contact, Contact: $contact"}
                    }

                    Debug.log {"Collision: ${a.id} hit ${b.id}"}
                }
            }
        }
        return contacts
    }


    private fun computeContact(
        entityA: String,
        entityB: String,
        colliderA: Collider,
        colliderB: Collider,
        transformA: Transform,
        transformB: Transform
    ): Contact? {
        return when {
            colliderA is CircleCollider && colliderB is CircleCollider -> {
                circleCircleContact(entityA, entityB, colliderA, colliderB, transformA, transformB)
            }
            colliderA is RectangleCollider && colliderB is CircleCollider -> {
                rectCircleContact(entityA, entityB, colliderA, colliderB, transformA, transformB)
            }
            colliderA is CircleCollider && colliderB is RectangleCollider -> {
                // Swap order so normal points A → B
                rectCircleContact(entityB, entityA, colliderB, colliderA, transformB, transformA)?.flipped()
            }
            colliderA is RectangleCollider && colliderB is RectangleCollider -> {
                rectRectContact(entityA, entityB, colliderA, colliderB, transformA, transformB)
            }
            else -> null
        }
    }

    private fun circleCircleContact(
        entityA: String, entityB: String,
        a: CircleCollider, b: CircleCollider,
        tA: Transform, tB: Transform
    ): Contact? {
        val normal = a.collisionNormal(b, tA, tB)
        val depth = a.penetrationDepth(b, tA, tB)
        if (depth <= 0.0) return null
        val point = a.pointOfContact(b, tA, tB) ?: tA.position
        return Contact(entityA, entityB, point, normal, depth)
    }

    private fun rectCircleContact(
        entityA: String, entityB: String,
        rect: RectangleCollider, circle: CircleCollider,
        tRect: Transform, tCircle: Transform
    ): Contact? {
        val closest = rect.closestPointTo(tCircle.position, tRect)
        val delta = tCircle.position - closest
        val dist = delta.length()
        val radius = circle.worldRadius(tCircle)
        val depth = radius - dist
        if (depth <= 0.0) return null

        val normal = if (dist < Vector2D.EPSILON_NORMALIZE) {
            // Circle center is exactly on the closest point — push along rect's Y axis
            Vector2D.UNIT_Y
        } else {
            delta.normalized()
        }

        return Contact(entityA, entityB, closest, normal, depth)
    }

    private fun rectRectContact(
        entityA: String, entityB: String,
        a: RectangleCollider, b: RectangleCollider,
        tA: Transform, tB: Transform
    ): Contact? {
        val cornersA = a.worldCorners(tA)
        val cornersB = b.worldCorners(tB)

        val axisAX = (cornersA[1] - cornersA[0]).normalized()
        val axisAY = (cornersA[3] - cornersA[0]).normalized()
        val axisBX = (cornersB[1] - cornersB[0]).normalized()
        val axisBY = (cornersB[3] - cornersB[0]).normalized()

        val axes = listOf(axisAX, axisAY, axisBX, axisBY)

        var bestDepth = Double.MAX_VALUE
        var bestNormal = Vector2D.UNIT_X

        for (axis in axes) {
            val (minA, maxA) = MathUtil.project(cornersA, axis)
            val (minB, maxB) = MathUtil.project(cornersB, axis)
            val overlap = minOf(maxA, maxB) - maxOf(minA, minB)

            if (overlap <= 0.0) return null // separated

            if (overlap < bestDepth) {
                bestDepth = overlap
                bestNormal = axis
            }
        }

        // Ensure normal points from A toward B
        val centerA = tA.position
        val centerB = tB.position
        if ((centerB - centerA).dot(bestNormal) < 0) {
            bestNormal = -bestNormal
        }

        // Contact point: midpoint of overlapping interval on the best axis
        val (minA, maxA) = MathUtil.project(cornersA, bestNormal)
        val (minB, maxB) = MathUtil.project(cornersB, bestNormal)
        val overlapStart = maxOf(minA, minB)
        val overlapEnd = minOf(maxA, maxB)
        val mid = (overlapStart + overlapEnd) * 0.5
        val point = bestNormal * mid // approximate, good enough for impulse resolution

        return Contact(entityA, entityB, point, bestNormal, bestDepth)
    }
}