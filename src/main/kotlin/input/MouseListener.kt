package org.soyuz.input

import org.soyuz.util.Vector2D
import org.lwjgl.glfw.GLFW.GLFW_PRESS
import org.lwjgl.glfw.GLFW.GLFW_RELEASE

object MouseListener {

    private var scrollX = 0.0
    private var scrollY = 0.0

    private var pos = Vector2D.ZERO
    private var lastPos = Vector2D.ZERO

    private val mouseButtonPressed = BooleanArray(3)
    private var isDragging = false

    fun mousePosCallback(windowLoc: Long, xpos: Double, ypos: Double) {
        lastPos = pos
        pos = Vector2D(xpos, ypos)

        isDragging = mouseButtonPressed.any { it }
    }

    fun mouseButtonCallback(windowLoc: Long, button: Int, action: Int, mods: Int) {
        if (button in mouseButtonPressed.indices) {
            if (action == GLFW_PRESS) {
                mouseButtonPressed[button] = true
            } else if (action == GLFW_RELEASE) {
                mouseButtonPressed[button] = false
                isDragging = false
            }
        }
    }

    fun mouseScrollCallback(windowLoc: Long, xOffset: Double, yOffset: Double) {
        scrollX = xOffset
        scrollY = yOffset
    }

    fun endFrame() {
        scrollX = 0.0
        scrollY = 0.0
        lastPos = pos
    }

    fun getX(): Float = pos.x.toFloat()
    fun getY(): Float = pos.y.toFloat()

    fun getPos(): Vector2D = pos

    fun getDX(): Float = (lastPos.x - pos.x).toFloat()
    fun getDY(): Float = (lastPos.y - pos.y).toFloat()

    fun getScrollX(): Float = scrollX.toFloat()
    fun getScrollY(): Float = scrollY.toFloat()

    fun isDragging(): Boolean = isDragging

    fun mouseButtonDown(button: Int): Boolean {
        return mouseButtonPressed.getOrElse(button) { false }
    }
}