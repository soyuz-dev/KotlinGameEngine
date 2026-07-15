package org.soyuz.util

import org.soyuz.util.math.Polynomial
import org.soyuz.util.math.times
import kotlin.test.*

class PolynomialTest {

    @Test
    fun `evaluate constant polynomial`() {
        val p = Polynomial(5.0)
        assertEquals(5.0, p.evaluate(0.0))
        assertEquals(5.0, p.evaluate(10.0))
        assertEquals(5.0, p.evaluate(-3.0))
    }

    @Test
    fun `evaluate linear polynomial`() {
        val p = Polynomial.linear(2.0, 3.0) // 2x + 3
        assertEquals(3.0, p.evaluate(0.0))
        assertEquals(5.0, p.evaluate(1.0))
        assertEquals(13.0, p.evaluate(5.0))
    }

    @Test
    fun `evaluate quadratic polynomial`() {
        val p = Polynomial.quadratic(1.0, 2.0, 3.0) // x² + 2x + 3
        assertEquals(3.0, p.evaluate(0.0))
        assertEquals(6.0, p.evaluate(1.0))
        assertEquals(18.0, p.evaluate(3.0))
    }

    @Test
    fun `evaluate via invoke operator`() {
        val p = Polynomial.linear(3.0, 1.0)
        assertEquals(7.0, p(2.0))
    }

    @Test
    fun `evaluate with vararg constructor`() {
        val p = Polynomial(1.0, 2.0, 3.0) // 1 + 2x + 3x²
        assertEquals(1.0, p(0.0))
        assertEquals(6.0, p(1.0))
    }

    @Test
    fun `addition of polynomials`() {
        val a = Polynomial(1.0, 2.0) // 1 + 2x
        val b = Polynomial(3.0, 4.0) // 3 + 4x
        val sum = a + b
        assertEquals(4.0, sum(0.0)) // 4
        assertEquals(10.0, sum(1.0)) // 4 + 6 = 10
    }

    @Test
    fun `addition with different lengths`() {
        val a = Polynomial(1.0, 2.0, 3.0) // 1 + 2x + 3x²
        val b = Polynomial(4.0) // 4
        val sum = a + b
        assertEquals(5.0, sum(0.0))
        assertEquals(5.0 + 2.0 + 3.0, sum(1.0))
    }

    @Test
    fun `subtraction of polynomials`() {
        val a = Polynomial(5.0, 3.0)
        val b = Polynomial(2.0, 1.0)
        val diff = a - b
        assertEquals(3.0, diff(0.0))
        assertEquals(5.0, diff(1.0))
    }

    @Test
    fun `scalar multiplication`() {
        val p = Polynomial(1.0, 2.0, 3.0)
        val scaled = p * 2.0
        assertEquals(2.0, scaled(0.0))
        assertEquals(12.0, scaled(1.0))
    }

    @Test
    fun `scalar multiplication from left`() {
        val p = Polynomial(1.0, 2.0)
        val scaled = 3.0 * p
        assertEquals(3.0, scaled(0.0))
        assertEquals(9.0, scaled(1.0))
    }

    @Test
    fun `polynomial multiplication`() {
        val a = Polynomial(1.0, 2.0) // 1 + 2x
        val b = Polynomial(3.0, 4.0) // 3 + 4x
        val prod = a * b
        // (1 + 2x)(3 + 4x) = 3 + 4x + 6x + 8x² = 3 + 10x + 8x²
        assertEquals(3.0, prod(0.0))
        assertEquals(21.0, prod(1.0))
        assertEquals(3.0 + 10.0 * 2.0 + 8.0 * 4.0, prod(2.0))
    }

    @Test
    fun `scalar division`() {
        val p = Polynomial(2.0, 4.0, 6.0)
        val div = p / 2.0
        assertEquals(1.0, div(0.0))
        assertEquals(6.0, div(1.0))
    }

    @Test
    fun `derivative of linear`() {
        val p = Polynomial.linear(3.0, 2.0) // 3x + 2
        val d = p.derivative()
        assertEquals(3.0, d(0.0))
        assertEquals(3.0, d(5.0))
    }

    @Test
    fun `derivative of quadratic`() {
        val p = Polynomial.quadratic(2.0, 3.0, 1.0) // 2x² + 3x + 1
        val d = p.derivative() // 4x + 3
        assertEquals(3.0, d(0.0))
        assertEquals(7.0, d(1.0))
    }

    @Test
    fun `derivative of constant is zero`() {
        val p = Polynomial.constant(5.0)
        val d = p.derivative()
        assertEquals(0.0, d(0.0))
        assertEquals(0.0, d(100.0))
    }

    @Test
    fun `integral of linear`() {
        val p = Polynomial.linear(2.0, 0.0) // 2x
        val integral = p.integral() // x² + C (C=0)
        assertEquals(0.0, integral(0.0))
        assertEquals(1.0, integral(1.0))
        assertEquals(4.0, integral(2.0))
    }

    @Test
    fun `integral with constant`() {
        val p = Polynomial.linear(3.0, 0.0) // 3x
        val integral = p.integral(5.0) // 1.5x² + 5
        assertEquals(5.0, integral(0.0))
        assertEquals(6.5, integral(1.0))
    }

    @Test
    fun `integral then derivative returns original`() {
        val p = Polynomial(1.0, 2.0, 3.0) // 1 + 2x + 3x²
        val result = p.integral().derivative()
        for (x in listOf(0.0, 1.0, 2.5, 10.0)) {
            assertEquals(p(x), result(x), 1e-9)
        }
    }

    @Test
    fun `companion constant factory`() {
        val p = Polynomial.constant(7.0)
        assertEquals(7.0, p(0.0))
        assertEquals(7.0, p(999.0))
    }

    @Test
    fun `companion linear factory`() {
        val p = Polynomial.linear(5.0, -2.0) // 5x - 2
        assertEquals(-2.0, p(0.0))
        assertEquals(3.0, p(1.0))
    }

    @Test
    fun `companion quadratic factory`() {
        val p = Polynomial.quadratic(1.0, 0.0, -4.0) // x² - 4
        assertEquals(-4.0, p(0.0))
        assertEquals(-3.0, p(1.0))
        assertEquals(0.0, p(2.0))
    }

    @Test
    fun `negative thrust field polynomial`() {
        val thrust = Polynomial(-0.5)
        assertEquals(-0.5, thrust(0.0))
        assertEquals(-0.5, thrust(100.0))
    }

    @Test
    fun `quadratic drag polynomial`() {
        val drag = Polynomial.quadratic(0.1) // 0.1x²
        assertEquals(0.0, drag(0.0), 1e-9)
        assertEquals(0.1, drag(1.0), 1e-9)
        assertEquals(0.4, drag(2.0), 1e-9)
    }
}