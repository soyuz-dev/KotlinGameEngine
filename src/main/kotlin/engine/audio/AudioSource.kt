package org.soyuz.engine.audio

import org.lwjgl.openal.AL11

class AudioSource {
    private val source = AL11.alGenSources()
    var volume = 1f; set(v) { field = v; AL11.alSourcef(source, AL11.AL_GAIN, v) }
    var pitch = 1f; set(v) { field = v; AL11.alSourcef(source, AL11.AL_PITCH, v) }
    var looping = false; set(v) { field = v; AL11.alSourcei(source, AL11.AL_LOOPING, if (v) 1 else 0) }

    fun play(clip: AudioClip) {
        AL11.alSourcei(source, AL11.AL_BUFFER, clip.buffer)
        AL11.alSourcePlay(source)
    }

    val isPlaying get() = AL11.alGetSourcei(source, AL11.AL_SOURCE_STATE) == AL11.AL_PLAYING

    fun pause() = AL11.alSourcePause(source)
    fun stop() = AL11.alSourceStop(source)
    fun cleanup() = AL11.alDeleteSources(source)
}