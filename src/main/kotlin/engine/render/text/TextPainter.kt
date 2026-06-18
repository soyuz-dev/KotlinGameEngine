package org.soyuz.engine.render.text

import org.soyuz.engine.render.Painter
import org.soyuz.engine.render.Shader
import org.soyuz.engine.render.image.Texture
import org.soyuz.util.Color
import org.soyuz.util.Dynamic

class TextPainter(
    val font: Font,
) : Painter, Dynamic {

    var text: String = ""
        set(value) {
            if (field != value) {
                field = value
                dirty = true
            }
        }

    var fontSize: Float = 20f
        set(value) {
            if (field != value) {
                field = value
                dirty = true
            }
        }

    var color: Color = Color(255,255,255)
        set(value) {
            if (field != value) {
                field = value
                dirty = true
            }
        }

    var texture: Texture? = null
    private var dirty = true

    override fun update(dt: Float) {
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
}