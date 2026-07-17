package org.soyuz.windowing

import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryUtil
import org.soyuz.input.KeyListener
import org.soyuz.input.MouseListener

class Window(
    title: String = "Bump",
    initialWidth: Int = 800,
    initialHeight: Int = 600,
    shareContext: Long = MemoryUtil.NULL
) {
    val handle: Long
    var width: Int = initialWidth
        set(value) {
            if (!inCallback) {
                GLFW.glfwSetWindowSize(handle, value, height)
            }
            field = value
        }
    var height: Int = initialHeight
        set(value) {
            if (!inCallback) {
                GLFW.glfwSetWindowSize(handle, width, value)
            }
            field = value
        }
    var x: Int = 0
        set(value) {
            field = value
            if (!inCallback) GLFW.glfwSetWindowPos(handle, value, y)
        }

    var y: Int = 0
        set(value) {
            field = value
            if (!inCallback) GLFW.glfwSetWindowPos(handle, x, value)
        }

    var title: String = title
    set(value) {
        GLFW.glfwSetWindowTitle(handle, value)
        field = value
    }
    private var inCallback = false

    init {
        handle = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, shareContext)
        if (handle == MemoryUtil.NULL) throw RuntimeException("Failed to create window")
        setupCallbacks()
    }

    fun configureOpenGlDefaults() {
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    }

    private fun setupCallbacks() {
        GLFW.glfwSetFramebufferSizeCallback(handle) { _, w, h ->
            inCallback = true
            width = w; height = h
            inCallback = false
        }
        GLFW.glfwSetWindowPosCallback(handle) { _, xpos, ypos ->
            inCallback = true
            x = xpos; y = ypos
            inCallback = false
        }
        GLFW.glfwSetKeyCallback(handle) { window, key, scancode, action, mods ->
            KeyListener.keyCallback(window, key, scancode, action, mods)
        }
        GLFW.glfwSetMouseButtonCallback(handle) { window, button, action, mods ->
            MouseListener.mouseButtonCallback(window, button, action, mods)
        }
        GLFW.glfwSetCursorPosCallback(handle) { window, xpos, ypos -> MouseListener.mousePosCallback(window, xpos, ypos) }
        GLFW.glfwSetScrollCallback(handle) { window, xoff, yoff -> MouseListener.mouseScrollCallback(window, xoff, yoff) }
    }

    fun setCharCallback(callback: (Char) -> Unit) {
        GLFW.glfwSetCharCallback(handle) { _, cp -> callback(cp.toChar()) }
    }

    fun makeContextCurrent() = GLFW.glfwMakeContextCurrent(handle)
    fun shouldClose() = GLFW.glfwWindowShouldClose(handle)
    fun swapBuffers() = GLFW.glfwSwapBuffers(handle)
    fun setVSync(enabled: Boolean) = GLFW.glfwSwapInterval(if (enabled) 1 else 0)
    fun quit() = GLFW.glfwSetWindowShouldClose(handle, true)
    fun minimize() = GLFW.glfwIconifyWindow(handle)
    fun restore() = GLFW.glfwRestoreWindow(handle)
    fun maximize() = GLFW.glfwMaximizeWindow(handle)
    fun isMinimized() = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE
    fun isMaximized() = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_MAXIMIZED) == GLFW.GLFW_TRUE
    fun isFocused() = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_FOCUSED) == GLFW.GLFW_TRUE

    fun center() {
        val monitor = GLFW.glfwGetPrimaryMonitor()
        val mode = GLFW.glfwGetVideoMode(monitor)!!
        x = (mode.width() - width)/2
        y = (mode.height() - height)/2
    }

    fun setFullscreen(fullscreen: Boolean) {
        if (fullscreen) {
            val monitor = GLFW.glfwGetPrimaryMonitor()
            val mode = GLFW.glfwGetVideoMode(monitor)!!
            GLFW.glfwSetWindowMonitor(handle, monitor, 0, 0, mode.width(), mode.height(), mode.refreshRate())
        } else {
            GLFW.glfwSetWindowMonitor(handle, MemoryUtil.NULL, 100, 100, width, height, GLFW.GLFW_DONT_CARE)
        }
    }

    fun destroy() {
        GLFW.glfwDestroyWindow(handle)
    }
}
