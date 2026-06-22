package org.soyuz.engine.ui

import org.soyuz.engine.entity.GameEntity
import org.soyuz.input.Input
import org.soyuz.util.Vector2D
import org.lwjgl.glfw.GLFW.*

object UISystem {
    private val states = mutableMapOf<String, UIState>()
    private var hoveredId: String? = null
    private var pressedId: String? = null
    private var focusedId: String? = null
    private var dragId: String? = null
    private var lastClickTime = 0.0
    private var lastClickId: String? = null
    private var lastClickButton = -1
    private var dragStartPos = Vector2D.ZERO
    private var lastMousePos = Vector2D.ZERO

    fun update(entities: List<GameEntity>) {
        val mousePos = Input.mousePosition()
        val mouseDelta = mousePos - lastMousePos
        lastMousePos = mousePos

        // Find topmost interactive entity under cursor
        val hit = entities
            .filter { it.interactive != null && it.painter != null }
            .lastOrNull { it.interactive!!.containsPoint(mousePos) }

        // --- Hover ---
        if (hit?.id != hoveredId) {
            hoveredId?.let { id ->
                entities.find { it.id == id }?.interactive?.onHoverExit()
                states[id]?.isHovered = false
            }
            hit?.let {
                it.interactive?.onHoverEnter()
                states.getOrPut(it.id) { UIState() }.isHovered = true
            }
            hoveredId = hit?.id
        }

        // --- Scroll ---
        val scroll = Input.scroll()
        if (scroll.x != 0.0 || scroll.y != 0.0) {
            val target = hit ?: focusedId?.let { id -> entities.find { it.id == id } }
            target?.interactive?.onScroll(scroll.x, scroll.y)
        }

        // --- Press ---
        if (Input.isMouseJustPressed(GLFW_MOUSE_BUTTON_LEFT)) {
            hit?.let {
                it.interactive?.onPress(GLFW_MOUSE_BUTTON_LEFT)
                states.getOrPut(it.id) { UIState() }.addPressedButton(GLFW_MOUSE_BUTTON_LEFT)
                pressedId = it.id
                dragStartPos = mousePos
            }
        }

        // --- Drag ---
        if (pressedId != null && Input.isMouseDown(GLFW_MOUSE_BUTTON_LEFT)) {
            val pressed = entities.find { it.id == pressedId }
            if (pressed != null) {
                if (dragId == null && mouseDelta.length() > 3.0) {
                    dragId = pressedId
                    pressed.interactive?.onDragStart(GLFW_MOUSE_BUTTON_LEFT, dragStartPos)
                    states[dragId]?.let {
                        it.isDragging = true
                        it.dragStartPos = dragStartPos
                        it.dragButton = GLFW_MOUSE_BUTTON_LEFT
                    }
                }
                if (dragId != null) {
                    pressed.interactive?.onDrag(mouseDelta, mousePos)
                }
            }
        }

        // --- Release ---
        if (Input.isMouseJustReleased(GLFW_MOUSE_BUTTON_LEFT)) {
            pressedId?.let { id ->
                val pressed = entities.find { it.id == id }
                pressed?.interactive?.onRelease(GLFW_MOUSE_BUTTON_LEFT)
                states[id]?.removePressedButton(GLFW_MOUSE_BUTTON_LEFT)
            }

            // Click
            if (pressedId != null && pressedId == hit?.id) {
                hit?.interactive?.onClick(GLFW_MOUSE_BUTTON_LEFT)

                // Double-click detection
                val now = glfwGetTime()
                if (pressedId == lastClickId &&
                    lastClickButton == GLFW_MOUSE_BUTTON_LEFT &&
                    now - lastClickTime < 0.3) {
                    hit?.interactive?.onDoubleClick(GLFW_MOUSE_BUTTON_LEFT)
                }
                lastClickTime = now
                lastClickId = pressedId
                lastClickButton = GLFW_MOUSE_BUTTON_LEFT

                // Focus
                if (pressedId != focusedId) {
                    focusedId?.let { fid ->
                        entities.find { it.id == fid }?.interactive?.onFocusLost()
                        states[fid]?.isFocused = false
                    }
                    hit?.interactive?.onFocusGained()
                    states[pressedId]?.isFocused = true
                    focusedId = pressedId
                }
            }

            // End drag
            if (dragId != null) {
                entities.find { it.id == dragId }?.interactive?.onDragEnd(GLFW_MOUSE_BUTTON_LEFT, mousePos)
                states[dragId]?.let {
                    it.isDragging = false
                    it.dragButton = -1
                }
                dragId = null
            }

            pressedId = null
        }

        // --- Keyboard ---
        // Route key events to focused entity
        val focused = focusedId?.let { id -> entities.find { it.id == id } }
        for (key in 0..348) {
            if (Input.isKeyJustPressed(key)) focused?.interactive?.onKeyPress(key)
            if (Input.isKeyJustReleased(key)) focused?.interactive?.onKeyRelease(key)
        }
    }

    fun getState(entityId: String): UIState = states.getOrPut(entityId) { UIState() }
}