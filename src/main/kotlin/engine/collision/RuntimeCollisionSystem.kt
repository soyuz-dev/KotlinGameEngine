package org.soyuz.engine.collision

import org.soyuz.engine.scene.Scene
import org.soyuz.util.MathUtil
import org.soyuz.util.Transform
import org.soyuz.util.Vector2D
import kotlin.math.abs

class RuntimeCollisionSystem : CollisionSystem {

    private val colliders = mutableMapOf<String, Collider>()


    override fun getCollider(entityId: String): Collider? = colliders[entityId]

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
                    }

                }
            }
        }
        return contacts
    }

    override fun hasCollider(id: String): Boolean {
        return id in colliders.keys
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
        var dist = delta.length()
        val radius = circle.worldRadius(tCircle)

        // 1. Calculate depth assuming standard outside-to-inside intersection
        var depth = radius - dist
        if (depth <= 0.0) return null

        var normal: Vector2D

        // 2. Safely handle deep penetration or exact overlaps
        if (dist < Vector2D.EPSILON_NORMALIZE) {
            // The circle center is perfectly on the edge, or deeply inside.
            // Derive normal pointing from the rectangle center to the circle center.
            val rectToCircle = tCircle.position - tRect.position
            if (rectToCircle.length() > Vector2D.EPSILON_NORMALIZE) {
                normal = rectToCircle.normalized()
            } else {
                // Perfectly overlapping centers fallback
                normal = Vector2D.UNIT_Y
            }
        } else {
            normal = delta.normalized()

            // 3. Double-check if the circle center is actually INSIDE the rectangle body
            // If it's inside, the normal vector needs to be inverted to push it OUT.
            val rectToCircle = tCircle.position - tRect.position
            if (rectToCircle.dot(normal) < 0) {
                normal = -normal
                // Adjust depth calculation if necessary for deep containment
                depth = radius + dist
            }
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

            if (overlap <= 0.0) return null

            if (overlap < bestDepth) {
                bestDepth = overlap
                bestNormal = axis
            }
        }

        // Find reference and incident edges
        val edgesA = getEdges(cornersA, tA.position)
        val edgesB = getEdges(cornersB, tB.position)

        val refEdge = edgesA.maxBy { it.normal.dot(bestNormal) }
        val incEdge = edgesB.maxBy { it.normal.dot(-bestNormal) }

        // Clip incident edge to reference edge
        val refDir = refEdge.end - refEdge.start
        val refLength = refDir.length()
        if (refLength < 1e-9) {
            // Degenerate edge, fall back
            val point = (tA.position + tB.position) * 0.5
            return Contact(entityA, entityB, point, bestNormal, bestDepth)
        }
        val refUnit = refDir / refLength

        val t1 = (incEdge.start - refEdge.start).dot(refUnit)
        val t2 = (incEdge.end - refEdge.start).dot(refUnit)
        val tMin = maxOf(0.0, minOf(t1, t2))
        val tMax = minOf(refLength, maxOf(t1, t2))

        val contactPoint = if (tMin >= tMax) {
            // Corner-edge
            if (abs(t1) < abs(t2 - refLength)) incEdge.start else incEdge.end
        } else {
            // Edge-edge
            refEdge.start + refUnit * ((tMin + tMax) * 0.5)
        }

        return Contact(entityA, entityB, contactPoint, bestNormal, bestDepth)
    }


    data class Edge(val start: Vector2D, val end: Vector2D, val normal: Vector2D)

    fun getEdges(corners: List<Vector2D>, center: Vector2D): List<Edge> {
        val edges = mutableListOf<Edge>()
        for (i in 0..3) {
            val start = corners[i]
            val end = corners[(i + 1) % 4]
            val edgeDir = end - start
            var normal = edgeDir.perpendicular().normalized()
            val mid = (start + end) * 0.5
            if ((mid - center).dot(normal) < 0) {
                normal = -normal
            }
            edges.add(Edge(start, end, normal))
        }
        return edges
    }
}