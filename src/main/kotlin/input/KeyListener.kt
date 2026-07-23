package org.soyuz.input

import org.lwjgl.glfw.GLFW.GLFW_PRESS
import org.lwjgl.glfw.GLFW.GLFW_RELEASE

object KeyListener {

    private val keyDown = mutableMapOf<Long, BooleanArray>()
    private val keyDownLast = mutableMapOf<Long, BooleanArray>()

    fun keyCallback(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
        val keys = keyDown.getOrPut(window) { BooleanArray(511) }
        if (key < 0 || key >= keys.size) return
        when (action) {
            GLFW_PRESS -> keys[key] = true
            GLFW_RELEASE -> keys[key] = false
        }
    }

    fun endFrame(window : Long) {
        val keys = keyDown.getOrPut(window) { BooleanArray(511) }
        val lastKeys = keyDownLast.getOrPut(window) { BooleanArray(511) }
        for (i in keys.indices) {
            lastKeys[i] = keys[i]
        }
    }

    fun isKeyDown(window:Long, key: Int): Boolean =
        keyDown[window]?.getOrElse(key) { false } ?: false

    fun isKeyJustPressed(window: Long, key: Int): Boolean =
        isKeyDown(window, key) && !(keyDownLast[window]?.getOrElse(key) { false } ?: false)

    fun isKeyJustReleased(window: Long, key: Int): Boolean =
        !isKeyDown(window, key) && (keyDownLast[window]?.getOrElse(key) { false } ?: false)
}