package org.soyuz.engine.audio

import org.lwjgl.openal.AL11
import org.lwjgl.stb.STBVorbis
import org.lwjgl.stb.STBVorbisInfo
import java.nio.ByteBuffer
import java.nio.ByteOrder


class AudioClip private constructor(val buffer: Int, val format: Int, val sampleRate: Int) {
    companion object {
        fun fromResource(path: String): AudioClip {
            val stream = AudioClip::class.java.getResourceAsStream(path)
                ?: throw IllegalArgumentException("Audio not found: $path")
            val bytes = stream.readAllBytes()
            val rawBuffer = ByteBuffer.allocateDirect(bytes.size)
            rawBuffer.put(bytes)
            rawBuffer.flip()

            val error = IntArray(1)
            val decoder = STBVorbis.stb_vorbis_open_memory(rawBuffer, error, null)
                ?: throw IllegalArgumentException("Failed to decode OGG: $path, error: ${error[0]}")

            val info = STBVorbisInfo.malloc()
            STBVorbis.stb_vorbis_get_info(decoder, info)

            val channels = info.channels()
            val sampleRate = info.sample_rate()

            val lengthPerChannel = STBVorbis.stb_vorbis_stream_length_in_samples(decoder)
            val pcm = ShortArray(lengthPerChannel * channels)
            val samplesDecodedPerChannel = STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm)

            STBVorbis.stb_vorbis_close(decoder)
            info.free()

            val alBuffer = AL11.alGenBuffers()
            val format = if (channels == 1) AL11.AL_FORMAT_MONO16 else AL11.AL_FORMAT_STEREO16

            val totalShorts = samplesDecodedPerChannel * channels
            val pcmBuffer = ByteBuffer.allocateDirect(totalShorts * 2)
            pcmBuffer.order(ByteOrder.LITTLE_ENDIAN)
            pcmBuffer.asShortBuffer().put(pcm, 0, totalShorts)
            pcmBuffer.rewind()  // ← Critical

            AL11.alBufferData(alBuffer, format, pcmBuffer, sampleRate)

            return AudioClip(alBuffer, format, sampleRate)
        }
    }

    fun cleanup() {
        AL11.alDeleteBuffers(buffer)
    }
}