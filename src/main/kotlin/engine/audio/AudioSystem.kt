package org.soyuz.engine.audio

import org.lwjgl.openal.AL
import org.lwjgl.openal.AL10
import org.lwjgl.openal.AL11
import org.lwjgl.openal.ALC
import org.lwjgl.openal.ALC11
import java.nio.ByteBuffer
import java.nio.IntBuffer
import kotlin.properties.Delegates

object AudioSystem {

    private var device by Delegates.notNull<Long>()
    private var context by Delegates.notNull<Long>()
    var initialised = false; private set


    fun init() {
        device = ALC11.alcOpenDevice(null as ByteBuffer?)
        context = ALC11.alcCreateContext(device, null as IntBuffer?)
        ALC11.alcMakeContextCurrent(context)
        AL.createCapabilities(ALC.createCapabilities(device))
        initialised = true
    }

    fun update(listenerX: Float, listenerY: Float, vX: Float, vY: Float) {
        AL11.alListener3f(AL11.AL_POSITION, listenerX, listenerY, 0f)
        AL11.alListener3f(AL11.AL_VELOCITY, vX, vY, 0f)
        // Orientation: at=(0,0,-1) up=(0,1,0) for 2D top-down
        AL11.alListenerfv(AL11.AL_ORIENTATION, floatArrayOf(0f, 0f, -1f, 0f, 1f, 0f))
    }

    fun cleanup() {
        ALC11.alcDestroyContext(context)
        ALC11.alcCloseDevice(device)
        initialised = false
    }

}