package org.soyuz.engine.render

import org.soyuz.engine.render.image.Texture
import org.soyuz.engine.render.text.Font
import org.soyuz.util.Color


class TextPainter(
    var text: String,
    val font: Font,
    var fontSize: Float,
    var color: Color
) : DynamicPainter {

    private var texture: Texture? = null
    private var dirty = true

    override fun update(dt: Double) {
        if (dirty) {
            texture?.cleanup()
            texture = font.rasterize(text, fontSize, color)
            dirty = false
        }
    }

    override fun bind(shader: Shader) {
        texture?.bind(0)
        shader.setTexture(0)
        shader.setUseTexture(true)
        shader.setColor(1f, 1f, 1f, 1f)
    }

    fun setText(newText: String) {
        if (newText != text) {
            text = newText
            dirty = true
        }
    }
}