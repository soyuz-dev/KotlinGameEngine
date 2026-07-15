package org.soyuz.engine.ui

import org.soyuz.util.math.Vector2D

data class UIState(
    var isHovered: Boolean = false,
    var isFocused: Boolean = false,
    val pressedButtons: MutableSet<Int> = mutableSetOf(),  // which buttons are currently down
    var isDragging: Boolean = false,
    var dragStartPos: Vector2D = Vector2D.ZERO,
    var dragButton: Int = -1,
    var lastClickTime: Double = 0.0,      // for double-click detection
    var lastClickButton: Int = -1
) {
    // Derive pressed state from pressedButtons set
    val isPressed: Boolean
        get() = pressedButtons.isNotEmpty()

    fun addPressedButton(button: Int) {
        pressedButtons.add(button)
    }

    fun removePressedButton(button: Int) {
        pressedButtons.remove(button)
    }

    fun isButtonPressed(button: Int): Boolean = button in pressedButtons

    fun reset() {
        isHovered = false
        isFocused = false
        pressedButtons.clear()
        isDragging = false
        dragStartPos = Vector2D.ZERO
        dragButton = -1
        lastClickTime = 0.0
        lastClickButton = -1
    }
}