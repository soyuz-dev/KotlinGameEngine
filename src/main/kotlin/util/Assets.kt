package org.soyuz.util

import org.lwjgl.glfw.GLFW.glfwGetCurrentContext
import org.soyuz.engine.audio.AudioClip
import org.soyuz.engine.render.Shader
import org.soyuz.engine.render.image.Texture
import org.soyuz.engine.render.text.Font

object Assets {

    private var warnedNoContext = false

    private val shaders = mutableMapOf<String, Shader>()
    private val textures = mutableMapOf<String, Texture>()
    private val audio = mutableMapOf<String, AudioClip>()

    private val fonts = mutableMapOf<String, Font>()

    fun shader(name: String): Shader {
        val key = "$ctx:$name"
        return shaders.getOrPut(key) {
            Shader.fromResource("/shaders/$name.vert", "/shaders/$name.frag")
        }
    }

    fun texture(name: String): Texture {
        val key = "$ctx:$name"
        return textures.getOrPut(key) {
            Texture.fromResource("/textures/$name.png")
        }
    }

    fun audio(name: String): AudioClip {
        return audio.getOrPut(name) {
            AudioClip.fromResource("/audio/$name.ogg")
        }
    }

    fun font(name: String): Font {
        val key = "$ctx:$name"
        return fonts.getOrPut(key) {
            Font("/fonts/$name.ttf")
        }
    }

    fun cleanup() {
        shaders.values.forEach { it.cleanup() }
        textures.values.forEach { it.cleanup() }
        audio.values.forEach { it.cleanup() }
        fonts.values.forEach { it.cleanup() }
        shaders.clear(); textures.clear(); audio.clear(); fonts.clear()
    }

    private val ctx: Long
        get() {
            val ctx = glfwGetCurrentContext()
            if (ctx == 0L && !warnedNoContext) {
                warnedNoContext = true
                Debug.log { "No current OpenGL context!" }
            }
            return ctx
        }
}