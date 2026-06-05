package org.soyuz.engine.collision

import org.soyuz.engine.shape.CircleShape
import org.soyuz.engine.shape.TriangleShape
import org.soyuz.util.Transform
import org.soyuz.util.Vector2D
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.*

class TriangleColliderTest {

    // Equilateral triangle: base=1.0, height ~0.866, centered roughly at origin
    // Vertices: top (0, 0.577), bottom-left (-0.5, -0.289), bottom-right (0.5, -0.289)
    private val h = 1.0 * sqrt(3.0) / 2.0  // ~0.866
    private val equiShape = TriangleShape(
        Vector2D(0.0, h * 2 / 3),          // top
        Vector2D(-0.5, -h / 3),           // bottom-left
        Vector2D(0.5, -h / 3)             // bottom-right
    )
    private val equiCollider = TriangleCollider(equiShape)

    private val bigEquiShape = TriangleShape(
        Vector2D(0.0, 2.0),
        Vector2D(-1.5, -1.0),
        Vector2D(1.5, -1.0)
    )
    private val bigEquiCollider = TriangleCollider(bigEquiShape)

    private val smallCircle = CircleCollider(CircleShape(0.2))
    private val bigCircle = CircleCollider(CircleShape(1.0))

    private val identity = Transform()

    // ==================== Triangle-Triangle (SAT) ====================

    @Test
    fun `identical triangles at same position intersect`() {
        assertTrue(equiCollider.intersects(equiCollider, identity, identity))
    }

    @Test
    fun `triangles separated by x axis do not intersect`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(5.0, 0.0))
        assertFalse(equiCollider.intersects(equiCollider, t1, t2))
    }

    @Test
    fun `triangles separated by y axis do not intersect`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(0.0, 5.0))
        assertFalse(equiCollider.intersects(equiCollider, t1, t2))
    }

    @Test
    fun `triangles overlapping partially intersect`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(0.3, 0.0)) // slight offset, still overlapping
        assertTrue(equiCollider.intersects(equiCollider, t1, t2))
    }

    @Test
    fun `small triangle inside big triangle intersects`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(0.0, 0.0))
        assertTrue(bigEquiCollider.intersects(equiCollider, t1, t2))
    }

    @Test
    fun `rotated triangles still detect overlap`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0), rotationRadians = Math.PI / 6)
        val t2 = Transform(position = Vector2D(0.5, 0.0))
        assertTrue(equiCollider.intersects(equiCollider, t1, t2))
    }

    @Test
    fun `rotated triangles separated do not intersect`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0), rotationRadians = Math.PI / 6)
        val t2 = Transform(position = Vector2D(5.0, 5.0), rotationRadians = -Math.PI / 4)
        assertFalse(equiCollider.intersects(equiCollider, t1, t2))
    }

    @Test
    fun `triangle touching at edge does not intersect`() {
        // Place two triangles so their edges just touch
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        // The equilateral triangle's rightmost point is x=0.5. Place another so its leftmost is x=0.5
        val t2 = Transform(position = Vector2D(1.0, 0.0))
        assertFalse(equiCollider.intersects(equiCollider, t1, t2))
    }

    @Test
    fun `symmetric intersection`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(0.2, 0.1))
        assertEquals(
            equiCollider.intersects(equiCollider, t1, t2),
            equiCollider.intersects(equiCollider, t2, t1)
        )
    }

    // ==================== Triangle-Circle ====================

    @Test
    fun `circle inside triangle intersects`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(0.0, 0.0)) // circle at center of triangle
        assertTrue(equiCollider.intersects(smallCircle, t1, t2))
    }

    @Test
    fun `circle outside triangle does not intersect`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(10.0, 0.0))
        assertFalse(equiCollider.intersects(smallCircle, t1, t2))
    }

    @Test
    fun `circle touching triangle edge intersects`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        // Bottom edge of equi is at y = -h/3 ≈ -0.289. Circle radius 0.2, center at y = -0.489
        val t2 = Transform(position = Vector2D(0.0, -0.48))
        assertTrue(equiCollider.intersects(smallCircle, t1, t2))
    }

    @Test
    fun `large circle overlaps triangle vertex`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(0.7, 0.7))
        assertTrue(equiCollider.intersects(bigCircle, t1, t2))
    }

    @Test
    fun `circle-triangle symmetric with CircleCollider`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(0.1, 0.0))
        assertTrue(equiCollider.intersects(smallCircle, t1, t2))
        assertTrue(smallCircle.intersects(equiCollider, t2, t1))
    }

    // ==================== Closest point ====================

    @Test
    fun `closestPointTo returns same point when inside triangle`() {
        val p = Vector2D(0.0, 0.0)
        val closest = equiCollider.closestPointTo(p, identity)
        assertEquals(p, closest)
    }

    @Test
    fun `closestPointTo returns point on edge for outside point`() {
        val p = Vector2D(2.0, 0.0)
        val closest = equiCollider.closestPointTo(p, identity)
        // Should be on the right edge of the triangle
        assertTrue(closest.x <= 0.5)
        assertTrue(closest.x >= 0.0)
    }

    @Test
    fun `closestPointTo returns vertex for far corner point`() {
        val p = Vector2D(-5.0, -5.0)
        val closest = equiCollider.closestPointTo(p, identity)
        // Should be the bottom-left vertex (-0.5, -h/3)
        assertEquals(-0.5, closest.x, 1e-9)
        assertEquals(-h / 3, closest.y, 1e-9)
    }

    @Test
    fun `closestPointTo works with translated triangle`() {
        val t = Transform(position = Vector2D(10.0, 20.0))
        val p = Vector2D(10.0, 20.0) // point at center of translated triangle
        val closest = equiCollider.closestPointTo(p, t)
        assertEquals(p, closest)
    }

    // ==================== Contains ====================

    @Test
    fun `contains returns true for point inside triangle`() {
        assertTrue(equiCollider.contains(Vector2D(0.0, 0.0), identity))
    }

    @Test
    fun `contains returns false for point outside triangle`() {
        assertFalse(equiCollider.contains(Vector2D(5.0, 0.0), identity))
    }

    @Test
    fun `contains returns true for vertex point`() {
        assertTrue(equiCollider.contains(Vector2D(0.0, h * 2 / 3), identity))
    }

    @Test
    fun `contains returns true for point on edge`() {
        // Midpoint of base
        assertTrue(equiCollider.contains(Vector2D(0.0, -h / 3), identity))
    }

    // ==================== World vertices ====================

    @Test
    fun `worldVertices returns 3 vertices`() {
        assertEquals(3, equiCollider.worldVertices(identity).size)
    }

    @Test
    fun `worldVertices translated`() {
        val t = Transform(position = Vector2D(5.0, 5.0))
        val verts = equiCollider.worldVertices(t)
        // Top vertex should be at (5, 5 + h*2/3)
        assertTrue(verts.any { it.x == 5.0 && abs(it.y - (5.0 + h * 2 / 3)) < 1e-9 })
    }

    // ==================== Penetration depth ====================

    @Test
    fun `penetrationDepth positive for overlapping triangles`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(0.1, 0.0))
        assertTrue(equiCollider.penetrationDepth(equiCollider, t1, t2) > 0.0)
    }

    @Test
    fun `penetrationDepth zero for separated triangles`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(100.0, 0.0))
        assertEquals(0.0, equiCollider.penetrationDepth(equiCollider, t1, t2), 1e-9)
    }

    // ==================== Collision normal ====================

    @Test
    fun `collisionNormal is unit length`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(0.1, 0.0))
        val normal = equiCollider.collisionNormal(equiCollider, t1, t2)
        assertEquals(1.0, normal.length(), 1e-9)
    }

    // ==================== Trigger ====================

    @Test
    fun `default isTrigger is false`() {
        assertFalse(equiCollider.isTrigger)
    }

    @Test
    fun `trigger flag can be set`() {
        val trigger = TriangleCollider(equiShape, isTrigger = true)
        assertTrue(trigger.isTrigger)
    }
}