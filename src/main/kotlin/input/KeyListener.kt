package org.soyuz.input

import org.lwjgl.glfw.GLFW.GLFW_PRESS

object KeyListener {
    private val keyPressed = BooleanArray(350)

    fun keyCallback(windowLoc: Long, key: Int, scancode: Int, action: Int, mods: Int) {
        if (key in keyPressed.indices) {
            keyPressed[key] = action == GLFW_PRESS
        }
    }

    fun isKeyPressed(keyCode: Int): Boolean {
        return keyPressed.getOrElse(keyCode) { false }
    }
}