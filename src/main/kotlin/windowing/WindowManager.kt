package org.soyuz.windowing

import org.lwjgl.glfw.GLFW.glfwGetTime
import org.lwjgl.glfw.GLFW.glfwPollEvents
import org.soyuz.engine.core.RuntimeEngine
import org.soyuz.input.KeyListener
import org.soyuz.input.MouseListener

class WindowManager {
    private val windows = mutableListOf<WindowRuntime>()
    private var lastTime = glfwGetTime()
    private var beforeLastWindowClosed: (() -> Unit)? = null

    fun add(
        window: Window,
        engine: RuntimeEngine,
        configure: WindowRuntime.() -> Unit = {}
    ): WindowRuntime {
        val runtime = WindowRuntime(window, engine)
        runtime.useContext()
        runtime.configure()
        windows += runtime
        return runtime
    }

    fun run() {
        windows.forEach { it.start() }

        while (windows.isNotEmpty()) {
            val currentTime = glfwGetTime()
            val dt = (currentTime - lastTime)
                .toFloat()
                .coerceIn(0f, RuntimeEngine.DEFAULT_MAX_FRAME_DELTA.toFloat())
            lastTime = currentTime

            glfwPollEvents()

            val iterator = windows.iterator()
            while (iterator.hasNext()) {
                val runtime = iterator.next()
                if (runtime.shouldClose()) {
                    cleanupBeforeLastClose(runtime)
                    runtime.close()
                    iterator.remove()
                } else {
                    runtime.frame(dt)
                }
            }

            windows.forEach {
                KeyListener.endFrame(it.window.handle)
                MouseListener.endFrame(it.window.handle)
            }

        }
    }

    fun closeAll() {
        windows.lastOrNull()?.let { runtime ->
            runtime.useContext()
            beforeLastWindowClosed?.invoke()
            beforeLastWindowClosed = null
        }
        windows.forEach { it.close() }
        windows.clear()
    }

    fun onBeforeLastWindowClosed(callback: () -> Unit) {
        beforeLastWindowClosed = callback
    }

    private fun cleanupBeforeLastClose(runtime: WindowRuntime) {
        if (windows.size != 1) return
        runtime.useContext()
        beforeLastWindowClosed?.invoke()
        beforeLastWindowClosed = null
    }
}
