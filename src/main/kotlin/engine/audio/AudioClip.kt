package org.soyuz.engine.audio

import org.lwjgl.openal.AL11
import java.nio.ByteBuffer


class AudioClip private constructor(val buffer: Int, val format: Int, val sampleRate: Int) {
    companion object {
        fun fromResource(path: String): AudioClip {
            val stream = AudioClip::class.java.getResourceAsStream(path)
                ?: throw IllegalArgumentException("Audio not found: $path")
            val bytes = stream.readAllBytes()
            val buffer = ByteBuffer.allocateDirect(bytes.size)
            buffer.put(bytes)
            buffer.flip()

            //RIFF
            if (buffer.getInt() != 0x52494646) error("Is your audio clip in WAV format?")
            buffer.getInt()

            //WAVE
            if (buffer.getInt() != 0x57415645) error("Is your audio clip in WAV format?")

            //fmt
            if (buffer.getInt() != 0x666d7420) error("Is your audio clip in WAV format?")

            val chunkSize = buffer.getInt()
            val audioFormat = buffer.getShort()
            val audioChannels = buffer.getShort()
            val sampleRate = buffer.getInt()

            buffer.getInt()
            buffer.getShort()
            if (buffer.getShort().toInt() != 16) error("Is your audio clip in WAV format?")

            // Skip any extra chunks until "data"
            while (buffer.getInt() != 0x64617461) {
                val skipSize = buffer.getInt()
                buffer.position(buffer.position() + skipSize)
            }
            val dataSize = buffer.getInt()

            // Create OpenAL buffer
            val alBuffer = AL11.alGenBuffers()
            val format = if (audioChannels == 1.toShort()) AL11.AL_FORMAT_MONO16 else AL11.AL_FORMAT_STEREO16
            AL11.alBufferData(alBuffer, format, buffer, sampleRate)

            return AudioClip(alBuffer, format, sampleRate)
        }
    }
}