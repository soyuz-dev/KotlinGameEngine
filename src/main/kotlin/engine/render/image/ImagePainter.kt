package org.soyuz.engine.render.image

import org.soyuz.engine.render.Painter
import org.soyuz.engine.render.Shader

class ImagePainter(
    private val texture: Texture,
    private val r: Double = 1.0,
    private val g: Double = 1.0,
    private val b: Double = 1.0,
    private val a: Double = 1.0
) : Painter {

    override fun bind(shader: Shader) {
        texture.bind(0)
        shader.setTexture(0)
        shader.setUseTexture(true)
        shader.setColor(r.toFloat(), g.toFloat(), b.toFloat(), a.toFloat())
    }
}