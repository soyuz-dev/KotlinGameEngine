package org.soyuz.engine.core

import org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR
import org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR
import org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE
import org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE
import org.lwjgl.glfw.GLFW.glfwDefaultWindowHints
import org.lwjgl.glfw.GLFW.glfwInit
import org.lwjgl.glfw.GLFW.glfwTerminate
import org.lwjgl.glfw.GLFW.glfwWindowHint
import org.soyuz.engine.audio.AudioSystem
import org.soyuz.util.Assets
import org.soyuz.windowing.WindowManager

class Application {
    val windows = WindowManager()
    private var running = false
    private var closed = false

    init {
        if (!glfwInit()) throw IllegalStateException("Unable to initialize GLFW")
        configureWindowHints()
        AudioSystem.init()
        windows.onBeforeLastWindowClosed { Assets.cleanup() }
    }

    fun run() {
        check(!closed) { "Application has already been closed." }
        if (running) return
        running = true

        try {
            windows.run()
        } finally {
            close()
        }
    }

    fun close() {
        if (closed) return
        running = false
        windows.closeAll()
        AudioSystem.cleanup()
        glfwTerminate()
        closed = true
    }

    private fun configureWindowHints() {
        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
    }
}
