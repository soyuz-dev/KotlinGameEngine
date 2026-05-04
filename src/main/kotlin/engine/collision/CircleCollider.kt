package org.soyuz.engine.collision

import org.soyuz.engine.entity.Transform
import org.soyuz.engine.shape.CircleShape
import org.soyuz.util.Debug
import org.soyuz.util.Vector2D
import kotlin.math.sqrt

class CircleCollider(
    val shape: CircleShape,
    override val isTrigger: Boolean = false
) : Collider {

    override fun intersects(
        other: Collider,
        selfTransform: Transform,
        otherTransform: Transform
    ): Boolean {
        return when (other) {
            is CircleCollider -> intersectsCircle(other, selfTransform, otherTransform)
            // Delegate to the other collider for mixed types
            // (once RectangleCollider exists, it will handle circle-rect)
            else -> other.intersects(this, otherTransform, selfTransform)
        }
    }

    private fun intersectsCircle(
        other: CircleCollider,
        selfTransform: Transform,
        otherTransform: Transform
    ): Boolean {
        val a = selfTransform.position
        val b = otherTransform.position

        val dx = b.x - a.x
        val dy = b.y - a.y
        val distanceSq = dx * dx + dy * dy

        // Account for scale (take the max component for circles; circles stay circular
        // unless non-uniform scale, but that's an edge case for advanced use)
        val radiusA = shape.radius.toDouble() * maxOf(
            kotlin.math.abs(selfTransform.scale.x),
            kotlin.math.abs(selfTransform.scale.y)
        )
        val radiusB = other.shape.radius.toDouble() * maxOf(
            kotlin.math.abs(otherTransform.scale.x),
            kotlin.math.abs(otherTransform.scale.y)
        )
        val radiusSum = radiusA + radiusB

        val result = distanceSq <= radiusSum * radiusSum

        Debug.log {
            "[CircleCollider] intersect test: $a vs $b | " +
                    "dist²=$distanceSq radiusSum²=${radiusSum * radiusSum} → $result"
        }

        return result
    }

    /**
     * Computes the world-space contact point on this collider's surface
     * in the direction toward the other collider.
     *
     * Returns null if the colliders are not overlapping.
     */
    fun pointOfContact(
        other: CircleCollider,
        selfTransform: Transform,
        otherTransform: Transform
    ): Vector2D? {
        val a = selfTransform.position
        val b = otherTransform.position

        val dx = b.x - a.x
        val dy = b.y - a.y
        val distSq = dx * dx + dy * dy

        if (distSq < Vector2D.EPSILON_NORMALIZE) {
            // Perfect overlap — return center
            Debug.log { "[CircleCollider] pointOfContact: perfect overlap at $a" }
            return a
        }

        val dist = sqrt(distSq)
        val radiusA = shape.radius.toDouble() * maxOf(
            kotlin.math.abs(selfTransform.scale.x),
            kotlin.math.abs(selfTransform.scale.y)
        )

        // Contact is on this circle's surface, pointing toward the other
        val nx = dx / dist
        val ny = dy / dist
        val contact = Vector2D(a.x + nx * radiusA, a.y + ny * radiusA)

        Debug.log { "[CircleCollider] pointOfContact: $contact (from $a toward $b)" }

        return contact
    }

    /**
     * Returns the nearest point on this collider's surface to a given world-space point.
     */
    fun nearestPointTo(
        point: Vector2D,
        transform: Transform
    ): Vector2D {
        val center = transform.position
        val dx = point.x - center.x
        val dy = point.y - center.y
        val distSq = dx * dx + dy * dy

        if (distSq < Vector2D.EPSILON_NORMALIZE) {
            // Point is at the center — return a point on the right edge
            val radius = shape.radius.toDouble() * maxOf(
                kotlin.math.abs(transform.scale.x),
                kotlin.math.abs(transform.scale.y)
            )
            return Vector2D(center.x + radius, center.y)
        }

        val dist = sqrt(distSq)
        val radius = shape.radius.toDouble() * maxOf(
            kotlin.math.abs(transform.scale.x),
            kotlin.math.abs(transform.scale.y)
        )
        val nx = dx / dist
        val ny = dy / dist
        return Vector2D(center.x + nx * radius, center.y + ny * radius)
    }

    /**
     * Tests whether a world-space point is inside this collider.
     */
    fun contains(point: Vector2D, transform: Transform): Boolean {
        val center = transform.position
        val radius = shape.radius.toDouble() * maxOf(
            kotlin.math.abs(transform.scale.x),
            kotlin.math.abs(transform.scale.y)
        )
        return (point - center).lengthSquared() <= radius * radius
    }

    /**
     * Computes the overlap depth (penetration) along the collision normal.
     * Returns 0.0 if no overlap.
     */
    fun penetrationDepth(
        other: CircleCollider,
        selfTransform: Transform,
        otherTransform: Transform
    ): Double {
        val a = selfTransform.position
        val b = otherTransform.position

        val dx = b.x - a.x
        val dy = b.y - a.y
        val dist = sqrt(dx * dx + dy * dy)

        val radiusA = shape.radius.toDouble() * maxOf(
            kotlin.math.abs(selfTransform.scale.x),
            kotlin.math.abs(selfTransform.scale.y)
        )
        val radiusB = other.shape.radius.toDouble() * maxOf(
            kotlin.math.abs(otherTransform.scale.x),
            kotlin.math.abs(otherTransform.scale.y)
        )
        val radiusSum = radiusA + radiusB

        val penetration = radiusSum - dist
        return if (penetration > 0.0) penetration else 0.0
    }

    /**
     * Collision normal pointing from this collider toward the other.
     * Returns unit vector, or Vector2D.UNIT_X if centers coincide.
     */
    fun collisionNormal(
        other: CircleCollider,
        selfTransform: Transform,
        otherTransform: Transform
    ): Vector2D {
        val a = selfTransform.position
        val b = otherTransform.position
        val delta = b - a

        return if (delta.isZero()) {
            Vector2D.UNIT_X // Arbitrary direction for overlapping centers
        } else {
            delta.normalize()
        }
    }
}