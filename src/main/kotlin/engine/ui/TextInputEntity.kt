package org.soyuz.engine.ui

import org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE
import org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE
import org.lwjgl.glfw.GLFW.GLFW_KEY_END
import org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER
import org.lwjgl.glfw.GLFW.GLFW_KEY_HOME
import org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT
import org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT
import org.soyuz.engine.collision.RectangleCollider
import org.soyuz.engine.entity.DefaultGameEntity
import org.soyuz.engine.render.CompositePainter
import org.soyuz.engine.render.Painter
import org.soyuz.engine.render.text.Font
import org.soyuz.engine.render.text.TextPainter
import org.soyuz.engine.shape.RectangleShape

class TextInputEntity(
    id: String,
    private val width: Double,
    font: Font,
    private val fontSize: Float,
    private val placeholder: String,
    val onSubmit: (String) -> Unit,
    val onTextChanged: (String) -> Unit = {},
) : DefaultGameEntity(id) {
    private val buffer = StringBuilder()
    private var cursorVisible = true
    private var focused = false
    private val textPainter: TextPainter

    private var cursorPos = 0

    val text: String get() {
        return buffer.toString()
    }

    init {
        shape = RectangleShape(width, fontSize * 1.5)

        textPainter = TextPainter(font)
        textPainter.fontSize = fontSize
        textPainter.text = placeholder
        textPainter.update(0f)  // force initial rasterize
        updateShape()

        painter = CompositePainter(textPainter)

        interactive = Interactive
            .clickable(containsPoint = {
                val result = collider!!.containsPoint(it, transform)
                result
            }) { }
            .focusable(
                onFocusGained = {
                    focused = true
                    updateDisplay()
                },
                onFocusLost = {
                    focused = false
                    updateDisplay()
                }
            )
            .keyboardInput(
                onKeyPress = { key -> handleKeyPress(key) },
                onCharTyped = { char -> handleChar(char) }
            )
    }

    private fun handleKeyPress(key: Int) {
        when (key) {
            GLFW_KEY_BACKSPACE -> {
                if (buffer.isNotEmpty() && cursorPos > 0) {
                    cursorPos--
                    buffer.deleteCharAt(cursorPos)
                    onTextChanged(buffer.toString())
                }
            }
            GLFW_KEY_ENTER -> {onSubmit(buffer.toString()); onTextChanged(buffer.toString())}
            GLFW_KEY_LEFT -> {
                if(cursorPos > 0) cursorPos--
            }
            GLFW_KEY_RIGHT -> {
                if(cursorPos < buffer.length) cursorPos++
            }
            GLFW_KEY_DELETE -> {
                if (cursorPos < buffer.length) {
                    buffer.deleteCharAt(cursorPos)
                    onTextChanged(buffer.toString())
                }
            }
            GLFW_KEY_HOME -> {
                cursorPos = 0
            }
            GLFW_KEY_END -> {
                cursorPos = buffer.length
            }
        }
        updateDisplay()
    }

    private fun handleChar(char: Char) {
        if (char.isISOControl()) return
        buffer.insert(cursorPos, char)
        cursorPos++
        updateDisplay()
        onTextChanged(buffer.toString())
    }
    private fun updateDisplay() {
        val displayText = if (buffer.isEmpty() && !focused) {
            placeholder
        } else {
            val before = buffer.substring(0, cursorPos)
            val after = buffer.substring(cursorPos)
            "$before${if (cursorVisible && focused) "|" else ""}$after"
        }
        textPainter.text = displayText
        textPainter.update(0f)
        updateShape()
    }

    fun toggleCursor() {
        cursorVisible = !cursorVisible
        updateDisplay()
    }

    private fun updateShape() {
        shape = RectangleShape(
            textPainter.texture?.width?.toDouble() ?: width,
            textPainter.texture?.height?.toDouble() ?: (fontSize * 1.5)
        )
        collider = RectangleCollider(shape as RectangleShape)
    }
}