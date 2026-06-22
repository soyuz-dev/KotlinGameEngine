package org.soyuz.engine.collision

import org.soyuz.engine.shape.TriangleShape
import org.soyuz.util.Transform
import org.soyuz.util.Vector2D


class TriangleCollider(
    val shape: TriangleShape,
    override val isTrigger: Boolean = false
) : Collider {

    override fun intersects(
        other: Collider,
        selfTransform: Transform,
        otherTransform: Transform
    ): Boolean {
        return when (other) {
            is TriangleCollider -> satTriangleTriangle(other, selfTransform, otherTransform)
            is CircleCollider -> triangleCircle(other, selfTransform, otherTransform)
            is RectangleCollider -> other.intersects(this, otherTransform, selfTransform)
            else -> other.intersects(this, otherTransform, selfTransform)
        }
    }

    // ==================== World vertices ====================

    fun worldVertices(transform: Transform): List<Vector2D> {
        return listOf(
            Transform.localToWorld(shape.a, transform),
            Transform.localToWorld(shape.b, transform),
            Transform.localToWorld(shape.c, transform)
        )
    }

    // ==================== Triangle-Triangle (SAT) ====================

    private fun satTriangleTriangle(
        other: TriangleCollider,
        tA: Transform,
        tB: Transform
    ): Boolean {
        val vertsA = worldVertices(tA)
        val vertsB = other.worldVertices(tB)

        val axes = mutableListOf<Vector2D>()
        // Edge normals from A
        for (i in 0..2) {
            axes.add(edgeNormal(vertsA[i], vertsA[(i + 1) % 3]))
        }
        // Edge normals from B
        for (i in 0..2) {
            axes.add(edgeNormal(vertsB[i], vertsB[(i + 1) % 3]))
        }

        for (axis in axes) {
            val (minA, maxA) = project(vertsA, axis)
            val (minB, maxB) = project(vertsB, axis)
            if (minOf(maxA, maxB) - maxOf(minA, minB) <= 0.0) return false
        }
        return true
    }

    private fun edgeNormal(from: Vector2D, to: Vector2D): Vector2D {
        return (to - from).perpendicular().normalized()
    }

    private fun project(verts: List<Vector2D>, axis: Vector2D): Pair<Double, Double> {
        var min = Double.MAX_VALUE
        var max = -Double.MAX_VALUE
        for (v in verts) {
            val proj = v.dot(axis)
            if (proj < min) min = proj
            if (proj > max) max = proj
        }
        return min to max
    }

    // ==================== Triangle-Circle ====================

    private fun triangleCircle(
        circle: CircleCollider,
        tTri: Transform,
        tCircle: Transform
    ): Boolean {
        val closest = closestPointTo(tCircle.position, tTri)
        val radius = circle.worldRadius(tCircle)
        return (tCircle.position - closest).lengthSquared() <= radius * radius
    }

    // ==================== Closest point on triangle ====================

    fun closestPointTo(point: Vector2D, transform: Transform): Vector2D {
        val verts = worldVertices(transform)

        // Check if point is inside triangle (barycentric)
        if (pointInTriangle(point, verts[0], verts[1], verts[2])) {
            return point
        }

        // Closest point on each edge
        var best = verts[0]
        var bestDistSq = (point - best).lengthSquared()

        for (i in 0..2) {
            val p = closestPointOnSegment(point, verts[i], verts[(i + 1) % 3])
            val distSq = (point - p).lengthSquared()
            if (distSq < bestDistSq) {
                bestDistSq = distSq
                best = p
            }
        }
        return best
    }

    private fun closestPointOnSegment(point: Vector2D, a: Vector2D, b: Vector2D): Vector2D {
        val ab = b - a
        val t = ((point - a).dot(ab) / ab.lengthSquared()).coerceIn(0.0, 1.0)
        return a + ab * t
    }

    private fun pointInTriangle(p: Vector2D, a: Vector2D, b: Vector2D, c: Vector2D): Boolean {
        val v0 = c - a
        val v1 = b - a
        val v2 = p - a

        val dot00 = v0.dot(v0)
        val dot01 = v0.dot(v1)
        val dot02 = v0.dot(v2)
        val dot11 = v1.dot(v1)
        val dot12 = v1.dot(v2)

        val invDenom = 1.0 / (dot00 * dot11 - dot01 * dot01)
        val u = (dot11 * dot02 - dot01 * dot12) * invDenom
        val v = (dot00 * dot12 - dot01 * dot02) * invDenom

        return u >= 0.0 && v >= 0.0 && u + v <= 1.0
    }

    // ==================== Contains ====================

    override fun containsPoint(point: Vector2D, transform: Transform): Boolean {
        val verts = worldVertices(transform)
        return pointInTriangle(point, verts[0], verts[1], verts[2])
    }

    // ==================== Penetration depth & normal ====================

    fun penetrationDepth(
        other: TriangleCollider,
        tA: Transform,
        tB: Transform
    ): Double {
        val vertsA = worldVertices(tA)
        val vertsB = other.worldVertices(tB)
        return satOverlap(vertsA, vertsB) ?: 0.0
    }

    fun collisionNormal(
        other: TriangleCollider,
        tA: Transform,
        tB: Transform
    ): Vector2D {
        val vertsA = worldVertices(tA)
        val vertsB = other.worldVertices(tB)
        return satNormal(vertsA, vertsB) ?: Vector2D.UNIT_X
    }

    private fun satOverlap(
        vertsA: List<Vector2D>,
        vertsB: List<Vector2D>
    ): Double? {
        var bestOverlap = Double.MAX_VALUE
        val allAxes = buildAxes(vertsA) + buildAxes(vertsB)
        for (axis in allAxes) {
            val (minA, maxA) = project(vertsA, axis)
            val (minB, maxB) = project(vertsB, axis)
            val overlap = minOf(maxA, maxB) - maxOf(minA, minB)
            if (overlap <= 0.0) return null
            if (overlap < bestOverlap) bestOverlap = overlap
        }
        return bestOverlap
    }

    private fun satNormal(
        vertsA: List<Vector2D>,
        vertsB: List<Vector2D>
    ): Vector2D? {
        var bestOverlap = Double.MAX_VALUE
        var bestAxis = Vector2D.UNIT_X
        val allAxes = buildAxes(vertsA) + buildAxes(vertsB)
        for (axis in allAxes) {
            val (minA, maxA) = project(vertsA, axis)
            val (minB, maxB) = project(vertsB, axis)
            val overlap = minOf(maxA, maxB) - maxOf(minA, minB)
            if (overlap <= 0.0) return null
            if (overlap < bestOverlap) {
                bestOverlap = overlap
                bestAxis = axis
            }
        }
        return bestAxis
    }

    private fun buildAxes(verts: List<Vector2D>): List<Vector2D> {
        return listOf(
            edgeNormal(verts[0], verts[1]),
            edgeNormal(verts[1], verts[2]),
            edgeNormal(verts[2], verts[0])
        )
    }

    override fun boundingCircle(transform: Transform): Pair<Vector2D, Double> {
        return transform.position to shape.supportRadius() * maxOf(
            kotlin.math.abs(transform.scale.x),
            kotlin.math.abs(transform.scale.y)
        )
    }
}