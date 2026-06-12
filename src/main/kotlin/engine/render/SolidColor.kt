package org.soyuz.engine.render

class SolidColor(
    val r:Double,
    val g:Double,
    val b:Double,
    val a:Double
) : Painter {
    override fun bind(shader: Shader) {
        shader.setUseTexture(false)
        shader.setColor(r.toFloat(), g.toFloat(), b.toFloat(), a.toFloat())
    }
}