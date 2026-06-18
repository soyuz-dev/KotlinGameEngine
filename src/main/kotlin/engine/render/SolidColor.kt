package org.soyuz.engine.render

import org.soyuz.util.Color

class SolidColor(
    val r:Double,
    val g:Double,
    val b:Double,
    val a:Double
) : Painter {

    constructor(color: Color) : this(
        r = color.r/255.0,
        g = color.g/255.0,
        b = color.b/255.0,
        a = color.a/255.0,
    )

    override fun bind(shader: Shader) {
        shader.setUseTexture(false)
        shader.setColor(r.toFloat(), g.toFloat(), b.toFloat(), a.toFloat())
    }
}