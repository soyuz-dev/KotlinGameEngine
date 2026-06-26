package org.soyuz.engine.ui

import org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE
import org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER
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
    background: Painter,
    private val placeholder: String,
    val onSubmit: (String) -> Unit
) : DefaultGameEntity(id) {
    private val buffer = StringBuilder()
    private var cursorVisible = true
    private var focused = false
    private val textPainter: TextPainter

    val text: String get() = buffer.toString()

    init {
        shape = RectangleShape(width, fontSize * 1.5)

        textPainter = TextPainter(font)
        textPainter.fontSize = fontSize
        textPainter.text = placeholder
        textPainter.update(0f)  // force initial rasterize
        updateShape()

        painter = CompositePainter(background, textPainter)

        interactive = Interactive
            .clickable(containsPoint = {
                val result = collider!!.containsPoint(it, transform)
                result
            }) { }
            .focusable(
                onFocusGained = {
                    println("FOCUS GAINED on $id")
                    focused = true
                    updateDisplay()
                },
                onFocusLost = {
                    println("FOCUS LOST on $id")
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
                if (buffer.isNotEmpty()) buffer.deleteCharAt(buffer.length - 1)
            }
            GLFW_KEY_ENTER -> onSubmit(buffer.toString())
        }
        updateDisplay()
    }

    private fun handleChar(char: Char) {
        println("char processed by textinput: $char")
        if (char.isISOControl()) return
        buffer.append(char)
        println("buffer: $buffer")
        updateDisplay()
    }

    private fun updateDisplay() {
        val displayText = if (buffer.isEmpty() && !focused) {
            placeholder
        } else {
            buffer.toString() + if (cursorVisible && focused) "|" else ""
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