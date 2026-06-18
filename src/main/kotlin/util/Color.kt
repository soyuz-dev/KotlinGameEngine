package org.soyuz.util

import kotlin.random.Random

data class Color(
    val r: Int,
    val g: Int,
    val b: Int,
    val a: Int = 255
) {
    companion object {
        fun random(): Color {
            val r = Random.nextInt(256)
            val g = Random.nextInt(256)
            val b = Random.nextInt(256)
            val a = 255
            return Color(r, g, b, a)
        }
    }
}