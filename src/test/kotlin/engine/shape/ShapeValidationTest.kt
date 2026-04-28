package org.soyuz.engine.shape

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ShapeValidationTest {

    @Test
    fun `circle rejects NaN radius`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            CircleShape(Float.NaN)
        }

        assertTrue(exception.message!!.contains("finite"))
    }

    @Test
    fun `circle rejects positive infinity radius`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            CircleShape(Float.POSITIVE_INFINITY)
        }

        assertTrue(exception.message!!.contains("finite"))
    }

    @Test
    fun `circle rejects zero radius`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            CircleShape(0f)
        }

        assertTrue(exception.message!!.contains("> 0"))
    }

    @Test
    fun `circle rejects negative radius`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            CircleShape(-1f)
        }

        assertTrue(exception.message!!.contains("> 0"))
    }

    @Test
    fun `rectangle rejects NaN width`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            RectangleShape(Float.NaN, 1f)
        }

        assertTrue(exception.message!!.contains("width must be finite"))
    }

    @Test
    fun `rectangle rejects infinity height`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            RectangleShape(1f, Float.NEGATIVE_INFINITY)
        }

        assertTrue(exception.message!!.contains("height must be finite"))
    }

    @Test
    fun `rectangle rejects zero width`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            RectangleShape(0f, 1f)
        }

        assertTrue(exception.message!!.contains("width must be > 0"))
    }

    @Test
    fun `rectangle rejects negative height`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            RectangleShape(1f, -0.01f)
        }

        assertTrue(exception.message!!.contains("height must be > 0"))
    }
}
