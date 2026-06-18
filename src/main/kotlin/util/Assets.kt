package org.soyuz.util

import org.soyuz.engine.audio.AudioClip
import org.soyuz.engine.render.Shader
import org.soyuz.engine.render.image.Texture
import org.soyuz.engine.render.text.Font

object Assets {
    private val shaders = mutableMapOf<String, Shader>()
    private val textures = mutableMapOf<String, Texture>()
    private val audio = mutableMapOf<String, AudioClip>()

    private val fonts = mutableMapOf<String, Font>()

    fun shader(name: String): Shader {
        return shaders.getOrPut(name) {
            Shader.fromResource("/shaders/$name.vert", "/shaders/$name.frag")
        }
    }

    fun texture(name: String): Texture {
        return textures.getOrPut(name) {
            Texture.fromResource("/textures/$name.png")
        }
    }

    fun audio(name: String): AudioClip {
        return audio.getOrPut(name) {
            AudioClip.fromResource("/audio/$name.ogg")
        }
    }

    fun font(name: String): Font {
        return fonts.getOrPut(name) {
            Font("/fonts/$name.ttf")
        }
    }

    fun cleanup() {
        shaders.values.forEach { it.cleanup() }
        textures.values.forEach { it.cleanup() }
        audio.values.forEach { it.cleanup() }
        shaders.clear(); textures.clear(); audio.clear()
    }
}