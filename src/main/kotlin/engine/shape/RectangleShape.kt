package org.soyuz.engine.shape

data class RectangleShape(
    val width: Float,
    val height: Float
) : Shape2D {
    override fun typeName(): String = TODO("Not yet implemented")
}
