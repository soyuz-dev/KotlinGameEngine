package org.soyuz.engine.shape

sealed interface Shape2D

data class CircleShape(
    val radius: Float
) : Shape2D

data class RectangleShape(
    val width: Float,
    val height: Float
) : Shape2D
