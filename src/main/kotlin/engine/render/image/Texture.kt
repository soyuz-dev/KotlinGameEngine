package org.soyuz.engine.render.image


import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.*
import org.lwjgl.stb.STBImage
import java.nio.ByteBuffer

class Texture private constructor(
    val id: Int,
    val width: Int,
    val height: Int
) {
    fun bind(slot: Int = 0) {
        glActiveTexture(GL_TEXTURE0 + slot)
        glBindTexture(GL_TEXTURE_2D, id)
    }

    constructor(width: Int, height: Int, pixels: ByteBuffer) : this(
        id = glGenTextures(),
        width = width,
        height = height
    ) {
        glBindTexture(GL_TEXTURE_2D, id)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
    }

    fun cleanup() {
        glDeleteTextures(id)
    }

    companion object {
        fun fromResource(path: String): Texture {
            val stream = Texture::class.java.getResourceAsStream(path)
                ?: throw IllegalArgumentException("Texture not found: $path")
            val bytes = stream.readAllBytes()
            val buffer = ByteBuffer.allocateDirect(bytes.size)
            buffer.put(bytes)
            buffer.flip()

            val x = IntArray(1); val y = IntArray(1); val comp = IntArray(1)
            val pixels = STBImage.stbi_load_from_memory(buffer, x, y, comp, 4)
                ?: throw IllegalArgumentException("Failed to load texture: $path, reason: ${STBImage.stbi_failure_reason()}")

            val id = glGenTextures()
            glBindTexture(GL_TEXTURE_2D, id)
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, x[0], y[0], 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

            STBImage.stbi_image_free(pixels)
            return Texture(id, x[0], y[0])
        }
    }
}