package org.soyuz.input

import org.soyuz.util.Vector2D
import org.lwjgl.glfw.GLFW.GLFW_PRESS
import org.lwjgl.glfw.GLFW.GLFW_RELEASE

object MouseListener {

    private var scrollX = 0.0
    private var scrollY = 0.0

    private var pos = Vector2D.ZERO
    private var lastPos = Vector2D.ZERO

    private val mouseDown = BooleanArray(3)
    private val mouseDownLast = BooleanArray(3)

    private var isDragging = false

    fun mousePosCallback(windowLoc: Long, xpos: Double, ypos: Double) {
        lastPos = pos
        pos = Vector2D(xpos, ypos)

        val moved = (pos.x != lastPos.x) || (pos.y != lastPos.y)
        isDragging = moved && mouseDown.any { it }
    }

    fun mouseButtonCallback(windowLoc: Long, button: Int, action: Int, mods: Int) {
        if (button in mouseDown.indices) {
            when (action) {
                GLFW_PRESS -> mouseDown[button] = true
                GLFW_RELEASE -> mouseDown[button] = false
            }
        }
    }

    fun mouseScrollCallback(windowLoc: Long, xOffset: Double, yOffset: Double) {
        scrollX += xOffset
        scrollY += yOffset
    }

    fun endFrame() {
        // store previous button states
        for (i in mouseDown.indices) {
            mouseDownLast[i] = mouseDown[i]
        }

        scrollX = 0.0
        scrollY = 0.0

        lastPos = pos
        isDragging = false
    }

    // --- Position ---

    fun getX(): Float = pos.x.toFloat()
    fun getY(): Float = pos.y.toFloat()

    fun getPos(): Vector2D = pos

    fun getDX(): Float = (pos.x - lastPos.x).toFloat()
    fun getDY(): Float = (pos.y - lastPos.y).toFloat()

    // --- Scroll ---

    fun getScrollX(): Float = scrollX.toFloat()
    fun getScrollY(): Float = scrollY.toFloat()

    // --- Buttons ---

    fun isMouseDown(button: Int): Boolean =
        mouseDown.getOrElse(button) { false }

    fun isMouseJustPressed(button: Int): Boolean =
        isMouseDown(button) && !mouseDownLast.getOrElse(button) { false }

    fun isMouseJustReleased(button: Int): Boolean =
        !isMouseDown(button) && mouseDownLast.getOrElse(button) { false }

    fun isDragging(): Boolean = isDragging
}