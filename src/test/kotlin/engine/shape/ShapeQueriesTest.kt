package org.soyuz.engine.shape

import org.soyuz.engine.entity.Transform
import org.soyuz.util.Vector2D
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShapeQueriesTest {

    @Test
    fun `worldAabb transforms rectangle local bounds`() {
        val transform = Transform(
            position = Vector2D(3.0, -1.0),
            rotationRadians = PI / 2,
            scale = Vector2D(2.0, 1.0)
        )

        val aabb = ShapeQueries.worldAabb(
            shape = RectangleShape(width = 4.0, height = 2.0),
            transform = transform
        )

        assertEquals(2.0, aabb.minX, 1e-9)
        assertEquals(-5.0, aabb.minY, 1e-9)
        assertEquals(4.0, aabb.maxX, 1e-9)
        assertEquals(3.0, aabb.maxY, 1e-9)
    }

    @Test
    fun `containsPoint projects world point through transform`() {
        val transform = Transform(
            position = Vector2D(10.0, 5.0),
            rotationRadians = PI / 2,
            scale = Vector2D(2.0, 1.0)
        )

        val shape = RectangleShape(width = 4.0, height = 2.0)

        assertTrue(ShapeQueries.containsPoint(shape, transform, Vector2D(9.5, 8.0)))
        assertFalse(ShapeQueries.containsPoint(shape, transform, Vector2D(11.2, 9.5)))
    }

    @Test
    fun `worldAabb handles non-uniform scaled circle with rotation`() {
        val transform = Transform(
            position = Vector2D(0.0, 0.0),
            rotationRadians = PI / 4,
            scale = Vector2D(2.0, 1.0)
        )

        val aabb = ShapeQueries.worldAabb(CircleShape(radius = 2.0), transform)

        assertEquals(-3.16227766, aabb.minX, 1e-6)
        assertEquals(-3.16227766, aabb.minY, 1e-6)
        assertEquals(3.16227766, aabb.maxX, 1e-6)
        assertEquals(3.16227766, aabb.maxY, 1e-6)
    }


}
