package org.soyuz.input

import org.soyuz.util.math.Vector2D

class Input(private val window: Long) {
    fun isKeyDown(key: Int) = KeyListener.isKeyDown(window, key)
    fun isKeyJustPressed(key: Int) = KeyListener.isKeyJustPressed(window, key)
    fun isKeyJustReleased(key: Int) = KeyListener.isKeyJustReleased(window, key)
    fun isMouseDown(button: Int) = MouseListener.isMouseDown(window, button)
    fun isMouseJustPressed(button: Int) = MouseListener.isMouseJustPressed(window, button)
    fun isMouseJustReleased(button: Int) = MouseListener.isMouseJustReleased(window, button)
    val mousePosition get() = MouseListener.getPos(window)
    val mouseDelta: Vector2D
        get() = Vector2D(
            MouseListener.getDX(window).toDouble(),
            MouseListener.getDY(window).toDouble()
        )
    val scroll: Vector2D
        get() = Vector2D(
            MouseListener.getScrollX(window).toDouble(),
            MouseListener.getScrollY(window).toDouble()
        )
}