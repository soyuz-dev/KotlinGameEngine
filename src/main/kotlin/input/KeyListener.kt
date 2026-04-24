package org.soyuz.input

import org.lwjgl.glfw.GLFW.GLFW_PRESS
import org.lwjgl.glfw.GLFW.GLFW_RELEASE

object KeyListener {

    private val keyDown = BooleanArray(350)
    private val keyDownLast = BooleanArray(350)

    fun keyCallback(windowLoc: Long, key: Int, scancode: Int, action: Int, mods: Int) {
        if (key in keyDown.indices) {
            when (action) {
                GLFW_PRESS -> keyDown[key] = true
                GLFW_RELEASE -> keyDown[key] = false
            }
        }
    }

    fun endFrame() {
        for (i in keyDown.indices) {
            keyDownLast[i] = keyDown[i]
        }
    }

    fun isKeyDown(key: Int): Boolean =
        keyDown.getOrElse(key) { false }

    fun isKeyJustPressed(key: Int): Boolean =
        isKeyDown(key) && !keyDownLast.getOrElse(key) { false }

    fun isKeyJustReleased(key: Int): Boolean =
        !isKeyDown(key) && keyDownLast.getOrElse(key) { false }
}