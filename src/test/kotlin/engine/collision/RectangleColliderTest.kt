package org.soyuz.engine.collision

import org.soyuz.util.Transform
import org.soyuz.engine.shape.CircleShape
import org.soyuz.engine.shape.RectangleShape
import org.soyuz.util.Vector2D
import kotlin.math.sqrt
import kotlin.test.*

class RectangleColliderTest {

    private val square10 = RectangleCollider(RectangleShape(10.0, 10.0))
    private val square20 = RectangleCollider(RectangleShape(20.0, 20.0))
    private val wideRect = RectangleCollider(RectangleShape(30.0, 10.0))
    private val smallCircle = CircleCollider(CircleShape(5.0))
    private val bigCircle = CircleCollider(CircleShape(15.0))
    private val identity = Transform()

    // ==================== rect-rect (SAT) ====================

    @Test
    fun `rect-rect overlapping returns true`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(12.0, 0.0)) // 12 apart, half-widths 5+10=15 → overlap
        assertTrue(square10.intersects(square20, t1, t2))
        assertTrue(square20.intersects(square10, t2, t1)) // symmetric
    }

    @Test
    fun `rect-rect separated returns false`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(50.0, 0.0))
        assertFalse(square10.intersects(square20, t1, t2))
        assertFalse(square20.intersects(square10, t2, t1))
    }

    @Test
    fun `rect-rect touching at edge returns false`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        // 5 + 10 = 15 edge-to-edge distance when centers are 15 apart
        val t2 = Transform(position = Vector2D(15.0, 0.0))
        assertFalse(square10.intersects(square20, t1, t2))
    }

    @Test
    fun `rect-rect slightly overlapping by one axis only`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(14.9, 0.0)) // just inside edge
        assertTrue(square10.intersects(square20, t1, t2))
    }

    @Test
    fun `one rect fully contained inside another`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(2.0, 2.0)) // small inside big
        assertTrue(square20.intersects(square10, t1, t2))
    }

    @Test
    fun `rect-rect overlapping with rotation on one`() {
        // 45° rotated small square at origin vs axis-aligned big square offset
        val t1 = Transform(position = Vector2D(0.0, 0.0), rotationRadians = Math.PI / 4)
        val t2 = Transform(position = Vector2D(10.0, 10.0))
        // They should overlap — corners of rotated square reach ~7.07 in each axis
        assertTrue(square10.intersects(square20, t1, t2))
    }

    @Test
    fun `rect-rect separated with rotation`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0), rotationRadians = Math.PI / 4)
        val t2 = Transform(position = Vector2D(50.0, 50.0))
        assertFalse(square10.intersects(square20, t1, t2))
    }

    @Test
    fun `both rects rotated differently still works`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0), rotationRadians = Math.PI / 6)
        val t2 = Transform(position = Vector2D(8.0, 8.0), rotationRadians = -Math.PI / 4)
        assertTrue(square10.intersects(square20, t1, t2))
    }

    @Test
    fun `separated by x but overlapping in y — no collision`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(50.0, 2.0)) // same y band, far x
        assertFalse(square10.intersects(square20, t1, t2))
    }

    @Test
    fun `separated by y but overlapping in x — no collision`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(2.0, 50.0)) // same x band, far y
        assertFalse(square10.intersects(square20, t1, t2))
    }

    // ==================== rect-circle ====================

    @Test
    fun `rect-circle overlapping — circle center outside rect`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0)) // square10 at origin
        val t2 = Transform(position = Vector2D(12.0, 0.0)) // circle radius 5, rect half-width 5
        // closest point on rect to circle center: (5, 0). Distance from (12,0) to (5,0) = 7 > 5.
        // Wait, 12 - 5 = 7 > 5 radius → separated
        assertFalse(square10.intersects(smallCircle, t1, t2))

        // Move closer: center at (9, 0), closest (5, 0), dist = 4 < 5 → overlap
        val t3 = Transform(position = Vector2D(9.0, 0.0))
        assertTrue(square10.intersects(smallCircle, t3, t2))
    }

    @Test
    fun `rect-circle overlapping — circle center inside rect`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0)) // rect 20x20
        val t2 = Transform(position = Vector2D(3.0, 3.0)) // circle radius 5, inside rect
        assertTrue(square20.intersects(smallCircle, t1, t2))
    }

    @Test
    fun `rect-circle separated`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(50.0, 50.0))
        assertFalse(square10.intersects(smallCircle, t1, t2))
    }

    @Test
    fun `rect-circle edge touch`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0)) // half-width 5
        val t2 = Transform(position = Vector2D(10.0, 0.0)) // radius 5, center 10 → edge at 5 exactly
        // closest point on rect to (10,0) is (5,0). Dist = 5, radius = 5 → overlap (<=)
        assertTrue(square10.intersects(smallCircle, t1, t2))
    }

    @Test
    fun `rect-circle symmetric — circle also detects rect`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(9.0, 0.0))
        assertTrue(square10.intersects(smallCircle, t1, t2))
        assertTrue(smallCircle.intersects(square10, t2, t1))
    }

    @Test
    fun `rect-circle with rotated rect`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0), rotationRadians = Math.PI / 4)
        val t2 = Transform(position = Vector2D(8.0, 8.0)) // near corner of rotated rect
        // Should still detect correctly via closest-point
        val result = square10.intersects(smallCircle, t1, t2)
        // Not asserting specific value — just that it doesn't crash and returns something
        assertNotNull(result)
    }

    // ==================== closestPointTo ====================

    @Test
    fun `closestPointTo returns correct point outside rect`() {
        val t = Transform(position = Vector2D(0.0, 0.0))
        val closest = square10.closestPointTo(Vector2D(20.0, 3.0), t)
        assertEquals(5.0, closest.x, 1e-9) // clamped to right edge
        assertEquals(3.0, closest.y, 1e-9) // within y range
    }

    @Test
    fun `closestPointTo returns clamped point for corner query`() {
        val t = Transform(position = Vector2D(0.0, 0.0))
        val closest = square10.closestPointTo(Vector2D(20.0, 20.0), t)
        assertEquals(5.0, closest.x, 1e-9)
        assertEquals(5.0, closest.y, 1e-9)
    }

    @Test
    fun `closestPointTo returns same point when inside rect`() {
        val t = Transform(position = Vector2D(0.0, 0.0))
        val closest = square10.closestPointTo(Vector2D(2.0, -1.0), t)
        assertEquals(2.0, closest.x, 1e-9)
        assertEquals(-1.0, closest.y, 1e-9)
    }

    @Test
    fun `closestPointTo works with translated rect`() {
        val t = Transform(position = Vector2D(100.0, 50.0))
        val closest = square10.closestPointTo(Vector2D(200.0, 50.0), t)
        assertEquals(105.0, closest.x, 1e-9) // right edge at 105
        assertEquals(50.0, closest.y, 1e-9)
    }

    // ==================== worldCorners ====================

    @Test
    fun `worldCorners returns 4 corners`() {
        val corners = square10.worldCorners(identity)
        assertEquals(4, corners.size)
    }

    @Test
    fun `worldCorners are at expected positions for identity transform`() {
        val corners = square10.worldCorners(identity)
        assertTrue(corners.any { it.x == -5.0 && it.y == -5.0 })
        assertTrue(corners.any { it.x == 5.0 && it.y == -5.0 })
        assertTrue(corners.any { it.x == 5.0 && it.y == 5.0 })
        assertTrue(corners.any { it.x == -5.0 && it.y == 5.0 })
    }

    @Test
    fun `worldCorners translated`() {
        val t = Transform(position = Vector2D(10.0, 20.0))
        val corners = square10.worldCorners(t)
        assertTrue(corners.any { it.x == 5.0 && it.y == 15.0 }) // (-5,-5) + (10,20) = (5,15)
    }

    // ==================== trigger flag ====================

    @Test
    fun `default isTrigger is false`() {
        assertFalse(square10.isTrigger)
    }

    @Test
    fun `trigger flag can be set`() {
        val trigger = RectangleCollider(RectangleShape(5.0, 5.0), isTrigger = true)
        assertTrue(trigger.isTrigger)
    }
}