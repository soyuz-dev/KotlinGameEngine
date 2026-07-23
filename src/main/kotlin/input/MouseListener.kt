package org.soyuz.input

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import org.lwjgl.glfw.GLFW.GLFW_PRESS
import org.lwjgl.glfw.GLFW.GLFW_RELEASE
import org.soyuz.util.math.Vector2D
import java.awt.MouseInfo

object MouseListener {
    private val positions = mutableMapOf<Long, Vector2D>()
    private val lastPositions = mutableMapOf<Long, Vector2D>()
    private val mouseDown = mutableMapOf<Long, BooleanArray>()
    private val mouseDownLast = mutableMapOf<Long, BooleanArray>()
    private val scrollX = mutableMapOf<Long, Double>()
    private val scrollY = mutableMapOf<Long, Double>()
    private val dragging = mutableMapOf<Long, Boolean>()

    fun mousePosCallback(window: Long, x: Double, y: Double) {
        val last = positions[window] ?: Vector2D(x, y)
        lastPositions[window] = last
        positions[window] = Vector2D(x, y)
        val moved = (x != last.x) || (y != last.y)
        val down = mouseDown[window]?.any { it } ?: false
        dragging[window] = moved && down
    }

    fun mouseButtonCallback(window: Long, button: Int, action: Int, mods: Int) {
        val buttons = mouseDown.getOrPut(window) { BooleanArray(3) }
        when (action) {
            GLFW_PRESS -> buttons[button] = true
            GLFW_RELEASE -> buttons[button] = false
        }
    }

    fun mouseScrollCallback(window: Long, x: Double, y: Double) {
        scrollX[window] = (scrollX[window] ?: 0.0) + x
        scrollY[window] = (scrollY[window] ?: 0.0) + y
    }

    fun endFrame(window: Long) {
        val buttons = mouseDown.getOrPut(window) { BooleanArray(3) }
        val lastButtons = mouseDownLast.getOrPut(window) { BooleanArray(3) }
        for (i in buttons.indices) {
            lastButtons[i] = buttons[i]
        }
        scrollX[window] = 0.0
        scrollY[window] = 0.0
        lastPositions[window] = positions[window] ?: Vector2D.ZERO
        dragging[window] = false
    }

    fun getPos(window: Long): Vector2D = positions[window] ?: Vector2D.ZERO
    fun getDX(window: Long): Float = ((positions[window]?.x ?: 0.0) - (lastPositions[window]?.x ?: 0.0)).toFloat()
    fun getDY(window: Long): Float = ((positions[window]?.y ?: 0.0) - (lastPositions[window]?.y ?: 0.0)).toFloat()
    fun isMouseDown(window: Long, button: Int): Boolean = mouseDown[window]?.getOrElse(button) { false } ?: false
    fun isMouseJustPressed(window: Long, button: Int): Boolean =
        isMouseDown(window, button) && !(mouseDownLast[window]?.getOrElse(button) { false } ?: false)
    fun isMouseJustReleased(window: Long, button: Int): Boolean =
        !isMouseDown(window, button) && (mouseDownLast[window]?.getOrElse(button) { false } ?: false)
    fun isDragging(window: Long): Boolean = dragging[window] ?: false
    fun getScrollX(window: Long): Float = (scrollX[window] ?: 0.0).toFloat()
    fun getScrollY(window: Long): Float = (scrollY[window] ?: 0.0).toFloat()

    fun getDesktopPos(): Vector2D {
        val p = MouseInfo.getPointerInfo().location
        return Vector2D(p.x.toDouble(), p.y.toDouble())
    }
}