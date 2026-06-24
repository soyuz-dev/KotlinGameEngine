package org.soyuz.engine.ui

import org.soyuz.util.Vector2D

interface Interactive {
    fun containsPoint(screenPoint: Vector2D): Boolean

    // Mouse buttons
    fun onPress(button: Int) = Unit
    fun onRelease(button: Int) = Unit
    fun onClick(button: Int) = Unit
    fun onDoubleClick(button: Int) = Unit

    // Hover
    fun onHoverEnter() = Unit
    fun onHoverExit() = Unit

    // Scroll
    fun onScroll(dx: Double, dy: Double) = Unit

    // Drag
    fun onDragStart(button: Int, startPos: Vector2D) = Unit
    fun onDrag(delta: Vector2D, currentPos: Vector2D) = Unit
    fun onDragEnd(button: Int, endPos: Vector2D) = Unit

    // Focus
    fun onFocusGained() = Unit
    fun onFocusLost() = Unit

    // Keyboard (for text input)
    fun onKeyPress(key: Int) = Unit
    fun onKeyRelease(key: Int) = Unit
    fun onCharTyped(char: Char) = Unit

    companion object {
        /**
         * Empty Interactive that does nothing—use as base for decorator chains.
         */
        fun empty(containsPoint: (Vector2D) -> Boolean): Interactive {
            return object : Interactive {
                override fun containsPoint(screenPoint: Vector2D) = containsPoint(screenPoint)
            }
        }

        // Factory methods for starting decorator chains
        fun clickable(
            containsPoint: (Vector2D) -> Boolean,
            onClick: () -> Unit
        ): Interactive = empty(containsPoint).clickable(onClick)

        fun hoverable(
            containsPoint: (Vector2D) -> Boolean,
            onHoverEnter: () -> Unit = {},
            onHoverExit: () -> Unit = {}
        ): Interactive = empty(containsPoint).hoverable(onHoverEnter, onHoverExit)

        fun draggable(
            containsPoint: (Vector2D) -> Boolean,
            onDragStart: (Int, Vector2D) -> Unit = { _, _ -> },
            onDrag: (Vector2D, Vector2D) -> Unit = { _, _ -> },
            onDragEnd: (Int, Vector2D) -> Unit = { _, _ -> }
        ): Interactive = empty(containsPoint).draggable(onDragStart, onDrag, onDragEnd)

        fun scrollable(
            containsPoint: (Vector2D) -> Boolean,
            onScroll: (Double, Double) -> Unit
        ): Interactive = empty(containsPoint).scrollable(onScroll)

        fun focusable(
            containsPoint: (Vector2D) -> Boolean,
            onFocusGained: () -> Unit = {},
            onFocusLost: () -> Unit = {}
        ): Interactive = empty(containsPoint).focusable(onFocusGained, onFocusLost)
    }
}

/**
 * Base decorator that delegates all methods to the wrapped Interactive.
 * Subclasses override only the methods they want to decorate.
 */
abstract class InteractiveDecorator(protected val base: Interactive) : Interactive {
    override fun containsPoint(screenPoint: Vector2D) = base.containsPoint(screenPoint)
    override fun onPress(button: Int) = base.onPress(button)
    override fun onRelease(button: Int) = base.onRelease(button)
    override fun onClick(button: Int) = base.onClick(button)
    override fun onDoubleClick(button: Int) = base.onDoubleClick(button)
    override fun onHoverEnter() = base.onHoverEnter()
    override fun onHoverExit() = base.onHoverExit()
    override fun onScroll(dx: Double, dy: Double) = base.onScroll(dx, dy)
    override fun onDragStart(button: Int, startPos: Vector2D) = base.onDragStart(button, startPos)
    override fun onDrag(delta: Vector2D, currentPos: Vector2D) = base.onDrag(delta, currentPos)
    override fun onDragEnd(button: Int, endPos: Vector2D) = base.onDragEnd(button, endPos)
    override fun onFocusGained() = base.onFocusGained()
    override fun onFocusLost() = base.onFocusLost()
    override fun onKeyPress(key: Int) = base.onKeyPress(key)
    override fun onKeyRelease(key: Int) = base.onKeyRelease(key)
    override fun onCharTyped(char: Char) = base.onCharTyped(char)
}

// ===== Decorator Extension Functions =====

fun Interactive.clickable(onClick: () -> Unit): Interactive {
    return object : InteractiveDecorator(this) {
        override fun onClick(button: Int) {
            onClick()
            base.onClick(button)
        }
    }
}

fun Interactive.hoverable(
    onHoverEnter: () -> Unit,
    onHoverExit: () -> Unit
): Interactive {
    return object : InteractiveDecorator(this) {
        override fun onHoverEnter() {
            onHoverEnter()
            base.onHoverEnter()
        }

        override fun onHoverExit() {
            onHoverExit()
            base.onHoverExit()
        }
    }
}

fun Interactive.draggable(
    onDragStart: (Int, Vector2D) -> Unit = { _, _ -> },
    onDrag: (Vector2D, Vector2D) -> Unit = { _, _ -> },
    onDragEnd: (Int, Vector2D) -> Unit = { _, _ -> }
): Interactive {
    return object : InteractiveDecorator(this) {
        override fun onDragStart(button: Int, startPos: Vector2D) {
            onDragStart(button, startPos)
            base.onDragStart(button, startPos)
        }

        override fun onDrag(delta: Vector2D, currentPos: Vector2D) {
            onDrag(delta, currentPos)
            base.onDrag(delta, currentPos)
        }

        override fun onDragEnd(button: Int, endPos: Vector2D) {
            onDragEnd(button, endPos)
            base.onDragEnd(button, endPos)
        }
    }
}

fun Interactive.scrollable(
    onScroll: (Double, Double) -> Unit
): Interactive {
    return object : InteractiveDecorator(this) {
        override fun onScroll(dx: Double, dy: Double) {
            onScroll(dx, dy)
            base.onScroll(dx, dy)
        }
    }
}

// Additional decorator functions for completeness

fun Interactive.focusable(
    onFocusGained: () -> Unit = {},
    onFocusLost: () -> Unit = {}
): Interactive {
    return object : InteractiveDecorator(this) {
        override fun onFocusGained() {
            onFocusGained()
            base.onFocusGained()
        }

        override fun onFocusLost() {
            onFocusLost()
            base.onFocusLost()
        }
    }
}

fun Interactive.keyboardInput(
    onKeyPress: (Int) -> Unit = {},
    onKeyRelease: (Int) -> Unit = {},
    onCharTyped: (Char) -> Unit = {}
): Interactive {
    return object : InteractiveDecorator(this) {
        override fun onKeyPress(key: Int) {
            onKeyPress(key)
            base.onKeyPress(key)
        }

        override fun onKeyRelease(key: Int) {
            onKeyRelease(key)
            base.onKeyRelease(key)
        }

        override fun onCharTyped(char: Char) {
            onCharTyped(char)
            base.onCharTyped(char)
        }
    }
}

// Chain decorators fluently
fun Interactive.doubleClickable(onDoubleClick: () -> Unit): Interactive {
    return object : InteractiveDecorator(this) {
        override fun onDoubleClick(button: Int) {
            onDoubleClick()
            base.onDoubleClick(button)
        }
    }
}
