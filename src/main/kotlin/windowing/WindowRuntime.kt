package org.soyuz.windowing

import org.lwjgl.glfw.GLFW.glfwMakeContextCurrent
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GLCapabilities
import org.soyuz.engine.core.RuntimeEngine

class WindowRuntime(
    val window: Window,
    val engine: RuntimeEngine
) {
    private val capabilities: GLCapabilities
    private var closed = false

    init {
        window.makeContextCurrent()
        capabilities = GL.createCapabilities()
        window.configureOpenGlDefaults()
        engine.init()
    }

    fun useContext() {
        window.makeContextCurrent()
        GL.setCapabilities(capabilities)
    }

    fun start() {
        engine.start()
    }

    fun frame(dt: Float) {
        if (closed || shouldClose()) return
        useContext()
        engine.update(dt)
        engine.renderCurrentContext()
        window.swapBuffers()
    }

    fun shouldClose(): Boolean = window.shouldClose()

    fun close() {
        if (closed) return
        useContext()
        engine.stop()
        GL.setCapabilities(null)
        glfwMakeContextCurrent(0L)
        window.destroy()
        closed = true
    }
}
