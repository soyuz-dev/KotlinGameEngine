package org.soyuz.input

import org.soyuz.util.Vector2D

class Input {

    // --- Keyboard ---
    fun isKeyDown(key: Int) = KeyListener.isKeyDown(key)
    fun isKeyJustPressed(key: Int) = KeyListener.isKeyJustPressed(key)
    fun isKeyJustReleased(key: Int) = KeyListener.isKeyJustReleased(key)

    // --- Mouse ---
    fun isMouseDown(button: Int) = MouseListener.isMouseDown(button)
    fun isMouseJustPressed(button: Int) = MouseListener.isMouseJustPressed(button)
    fun isMouseJustReleased(button: Int) = MouseListener.isMouseJustReleased(button)

    fun mousePosition() = MouseListener.getPos()
    fun mouseDelta() = Vector2D(MouseListener.getDX().toDouble(), MouseListener.getDY().toDouble())

    fun scroll() = Vector2D(
        MouseListener.getScrollX().toDouble(),
        MouseListener.getScrollY().toDouble()
    )
}