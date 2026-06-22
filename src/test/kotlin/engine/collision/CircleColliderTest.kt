package org.soyuz.engine.collision

import org.soyuz.util.Transform
import org.soyuz.engine.shape.CircleShape
import org.soyuz.util.Vector2D
import kotlin.test.*

class CircleColliderTest {

    private val smallCircle = CircleCollider(CircleShape(10.0))
    private val bigCircle = CircleCollider(CircleShape(20.0))
    private val tinyCircle = CircleCollider(CircleShape(1.0))

    private val identity = Transform()

    // --- intersects ---

    @Test
    fun `intersects true for overlapping circles`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(25.0, 0.0)) // 25 apart, radii 10+20=30 → overlap
        assertTrue(smallCircle.intersects(bigCircle, t1, t2))
        assertTrue(bigCircle.intersects(smallCircle, t2, t1)) // symmetric
    }

    @Test
    fun `intersects false for separated circles`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(50.0, 0.0)) // 50 apart, radii 10+20=30 → no overlap
        assertFalse(smallCircle.intersects(bigCircle, t1, t2))
        assertFalse(bigCircle.intersects(smallCircle, t2, t1))
    }

    @Test
    fun `intersects true when circles touch at edge`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(30.0, 0.0)) // exactly radiusSum
        assertTrue(smallCircle.intersects(bigCircle, t1, t2))
    }

    @Test
    fun `intersects true when one circle fully contains another`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(5.0, 0.0)) // small inside big, centers 5 apart
        assertTrue(bigCircle.intersects(smallCircle, t1, t2))
    }

    @Test
    fun `intersects true when circles share center`() {
        val t = Transform(position = Vector2D(0.0, 0.0))
        assertTrue(smallCircle.intersects(bigCircle, t, t))
    }

    @Test
    fun `intersects accounts for scale`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0), scale = Vector2D(2.0, 2.0)) // radius = 20
        val t2 = Transform(position = Vector2D(50.0, 0.0)) // radius = 20
        // 50 apart, radii 20+20=40 → separated
        assertFalse(smallCircle.intersects(bigCircle, t1, t2))

        val t3 = Transform(position = Vector2D(35.0, 0.0), scale = Vector2D(2.0, 2.0)) // radius = 20
        // 35 apart, radii 20+20=40 → overlap
        assertTrue(smallCircle.intersects(bigCircle, t3, t2))
    }

    @Test
    fun `intersects with non-uniform scale uses max component`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0), scale = Vector2D(3.0, 1.0)) // radius = 30
        val t2 = Transform(position = Vector2D(40.0, 0.0)) // radius = 20
        // 40 apart, radii 30+20=50 → overlap
        assertTrue(smallCircle.intersects(bigCircle, t1, t2))
    }

    // --- penetrationDepth ---

    @Test
    fun `penetrationDepth positive for overlapping circles`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(20.0, 0.0)) // 20 apart, radii 10+20=30 → pen = 10
        assertEquals(10.0, smallCircle.penetrationDepth(bigCircle, t1, t2), 1e-9)
    }

    @Test
    fun `penetrationDepth zero for separated circles`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(100.0, 0.0))
        assertEquals(0.0, smallCircle.penetrationDepth(bigCircle, t1, t2), 1e-9)
    }

    @Test
    fun `penetrationDepth zero when just touching`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(30.0, 0.0)) // exactly radiusSum
        assertEquals(0.0, smallCircle.penetrationDepth(bigCircle, t1, t2), 1e-9)
    }

    // --- collisionNormal ---

    @Test
    fun `collisionNormal points from self toward other`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(30.0, 0.0))
        val normal = smallCircle.collisionNormal(bigCircle, t1, t2)
        assertEquals(Vector2D(1.0, 0.0), normal)
    }

    @Test
    fun `collisionNormal is unit length`() {
        val t1 = Transform(position = Vector2D(10.0, 20.0))
        val t2 = Transform(position = Vector2D(60.0, 80.0))
        val normal = smallCircle.collisionNormal(bigCircle, t1, t2)
        assertEquals(1.0, normal.length(), 1e-9)
    }

    @Test
    fun `collisionNormal returns UNIT_X for coincident centers`() {
        val t = Transform(position = Vector2D(5.0, 5.0))
        val normal = smallCircle.collisionNormal(bigCircle, t, t)
        assertEquals(Vector2D.UNIT_X, normal)
    }

    // --- pointOfContact ---

    @Test
    fun `pointOfContact returns point on this surface toward other`() {
        val t1 = Transform(position = Vector2D(0.0, 0.0))
        val t2 = Transform(position = Vector2D(30.0, 0.0))
        val contact = smallCircle.pointOfContact(bigCircle, t1, t2)
        assertNotNull(contact)
        assertEquals(10.0, contact.x, 1e-9) // on surface of small circle (radius 10)
        assertEquals(0.0, contact.y, 1e-9)
    }

    @Test
    fun `pointOfContact returns center for coincident centers`() {
        val t = Transform(position = Vector2D(5.0, 5.0))
        val contact = smallCircle.pointOfContact(bigCircle, t, t)
        assertEquals(Vector2D(5.0, 5.0), contact)
    }

    // --- contains ---

    @Test
    fun `contains returns true for point inside circle`() {
        val t = Transform(position = Vector2D(0.0, 0.0))
        assertTrue(smallCircle.containsPoint(Vector2D(5.0, 0.0), t))
    }

    @Test
    fun `contains returns false for point outside circle`() {
        val t = Transform(position = Vector2D(0.0, 0.0))
        assertFalse(smallCircle.containsPoint(Vector2D(15.0, 0.0), t))
    }

    @Test
    fun `contains returns true for point exactly on boundary`() {
        val t = Transform(position = Vector2D(0.0, 0.0))
        assertTrue(smallCircle.containsPoint(Vector2D(10.0, 0.0), t))
    }

    // --- nearestPointTo ---

    @Test
    fun `nearestPointTo returns point on surface toward query point`() {
        val t = Transform(position = Vector2D(0.0, 0.0))
        val nearest = smallCircle.nearestPointTo(Vector2D(100.0, 0.0), t)
        assertEquals(10.0, nearest.x, 1e-9)
        assertEquals(0.0, nearest.y, 1e-9)
    }

    @Test
    fun `nearestPointTo returns edge point for query at center`() {
        val t = Transform(position = Vector2D(0.0, 0.0))
        val nearest = smallCircle.nearestPointTo(Vector2D(0.0, 0.0), t)
        assertEquals(10.0, nearest.x, 1e-9)
        assertEquals(0.0, nearest.y, 1e-9)
    }

    // --- isTrigger ---

    @Test
    fun `default isTrigger is false`() {
        assertFalse(smallCircle.isTrigger)
    }

    @Test
    fun `trigger flag can be set`() {
        val trigger = CircleCollider(CircleShape(5.0), isTrigger = true)
        assertTrue(trigger.isTrigger)
    }
}