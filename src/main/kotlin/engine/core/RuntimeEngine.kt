package org.soyuz.engine.core

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL
import org.soyuz.engine.audio.AudioSystem
import org.soyuz.engine.physics.PhysicsSystem
import org.soyuz.engine.render.Camera
import org.soyuz.engine.render.RenderSystem
import org.soyuz.engine.render.Shader
import org.soyuz.engine.scene.Scene
import org.soyuz.engine.ui.UISystem
import org.soyuz.input.KeyListener
import org.soyuz.input.MouseListener
import org.soyuz.util.Assets
import org.soyuz.util.Dynamic

class RuntimeEngine(
    title: String = "Bump",
    private var windowWidth: Int = 800,
    private var windowHeight: Int = 600,
    private val physicsSystem: PhysicsSystem?,
    private val camera: Camera
) : Engine, Dynamic {


    var title: String = title
        set(value) {
            field = value
            glfwSetWindowTitle(window, value)
        }



    lateinit var renderSystem: RenderSystem
    lateinit var shader: Shader

    var window: Long = NULL
        private set
    var width: Int = windowWidth
        set(value) {
            field = value
            glfwSetWindowSize(window, value, height)
        }
    var height: Int = windowHeight
        set(value) {
            field = value
            glfwSetWindowSize(window, width, value)
        }

    private var currentScene: Scene? = null
    private var running = false
    private var accumulator = 0.0
    private val timers = mutableListOf<Timer>()

    private data class Timer(
        val intervalMs: Double,
        var accumulator: Double = 0.0,
        val type: TimerType = TimerType.INTERVAL,
        val callback: (progress: Double) -> Unit
    )

    private enum class TimerType {
        INTERVAL, ONE_SHOT, DURING, FOREVER
    }

    /**
     * Initializes the GLFW Window, OpenGL context, and Audio systems.
     * Call this before loading assets or running the engine.
     */
    fun init() {
        if (window != NULL) return // Already initialized

        if (!glfwInit()) throw IllegalStateException("Unable to initialize GLFW")

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)

        window = glfwCreateWindow(width, height, title, NULL, NULL)
        if (window == NULL) throw RuntimeException("Failed to create window")

        glfwMakeContextCurrent(window)
        GL.createCapabilities()
        glfwSwapInterval(0)

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        camera.setOrtho(width.toFloat(), height.toFloat())
        AudioSystem.init()

        setupCallbacks()
    }

    private fun setupCallbacks() {
        glfwSetFramebufferSizeCallback(window) { _, w, h ->
            width = w
            height = h
            glViewport(0, 0, w, h)
            camera.setOrtho(w.toFloat(), h.toFloat())
        }
        glfwSetKeyCallback(window) { _, key, _, action, _ ->
            KeyListener.keyCallback(0, key, 0, action, 0)
        }
        glfwSetMouseButtonCallback(window) { _, button, action, _ ->
            MouseListener.mouseButtonCallback(0, button, action, 0)
        }
        glfwSetCursorPosCallback(window) { _, x, y ->
            MouseListener.mousePosCallback(0, x, y)
        }
        glfwSetScrollCallback(window) { _, x, y ->
            MouseListener.mouseScrollCallback(0, x, y)
        }
        glfwSetCharCallback(window) { _, codepoint ->
            onChar(codepoint.toChar())
        }
    }

    fun onChar(char: Char) {
        val scene = currentScene ?: return
        UISystem.handleChar(char, scene.allEntities())
    }

    fun forEvery(ms: Double, callback: () -> Unit) {
        timers.add(Timer(intervalMs = ms, type = TimerType.INTERVAL) { callback() })
    }

    fun forever(callback: (dt: Double) -> Unit) {
        timers.add(Timer(intervalMs = 0.0, type = TimerType.FOREVER) { callback(it) })
    }

    fun after(ms: Double, callback: () -> Unit) {
        timers.add(Timer(intervalMs = ms, type = TimerType.ONE_SHOT) { callback() })
    }

    fun during(ms: Double, callback: (progress: Double) -> Unit) {
        timers.add(Timer(intervalMs = ms, type = TimerType.DURING, callback = callback))
    }

    override fun loadScene(scene: Scene) {
        currentScene?.cleanup()
        currentScene = scene
        accumulator = 0.0
        if (running) {
            scene.init()
        }
    }

    override fun start() {
        if (running) return
        if (window == NULL) {
            throw IllegalStateException("Engine must be initialized via init() before starting.")
        }
        // Safety check to ensure lateinit properties were assigned after init()
        if (!::renderSystem.isInitialized || !::shader.isInitialized) {
            throw IllegalStateException("renderSystem and shader must be assigned before starting the engine.")
        }

        running = true
        accumulator = 0.0
        currentScene?.init()
    }

    override fun stop() {
        if (!running) return
        running = false
        currentScene?.cleanup()
        AudioSystem.cleanup()
        Assets.cleanup()
    }

    private var dt = 0.0

    override fun update(dt: Float) {
        if (!running || dt <= 0f || !dt.isFinite()) return
        val scene = currentScene ?: return

        this.dt += dt.toDouble()

        while (this.dt >= DEFAULT_PHYSICS_TIMESTEP) {
            physicsSystem?.step(scene, DEFAULT_PHYSICS_TIMESTEP)
            this.dt -=DEFAULT_PHYSICS_TIMESTEP
        }

        val entities = scene.allEntities()
        UISystem.update(entities)

        entities.forEach { entity ->
            (entity.painter as? Dynamic)?.update(dt)
        }

        val dtMs = dt * 1000.0
        timers.removeAll { timer ->
            timer.accumulator += dtMs

            when (timer.type) {
                TimerType.INTERVAL -> {
                    while (timer.accumulator >= timer.intervalMs) {
                        timer.accumulator -= timer.intervalMs
                        timer.callback(0.0)
                    }
                    false
                }
                TimerType.ONE_SHOT -> {
                    if (timer.accumulator >= timer.intervalMs) {
                        timer.callback(1.0)
                        true
                    } else {
                        false
                    }
                }
                TimerType.DURING -> {
                    if (timer.accumulator < timer.intervalMs) {
                        val progress = (timer.accumulator / timer.intervalMs).coerceIn(0.0, 1.0)
                        timer.callback(progress)
                        false
                    } else {
                        timer.callback(1.0)
                        true
                    }
                }
                TimerType.FOREVER -> {
                    timer.callback(dt.toDouble())
                    false
                }
            }
        }
    }

    fun render() {
        if (!running) return
        val scene = currentScene ?: return
        renderSystem.render(scene, camera, shader)
    }

    override fun run() {
        start()

        var lastTime = glfwGetTime()

        while (!glfwWindowShouldClose(window)) {
            val currentTime = glfwGetTime()
            val dt = (currentTime - lastTime).toFloat().coerceIn(0f, DEFAULT_MAX_FRAME_DELTA.toFloat())
            lastTime = currentTime

            glfwPollEvents()
            update(dt)
            render()
            glfwSwapBuffers(window)

            KeyListener.endFrame()
            MouseListener.endFrame()
        }

        stop()
        glfwDestroyWindow(window)
        glfwTerminate()
    }

    companion object {
        const val DEFAULT_MAX_FRAME_DELTA: Double = 0.25

        const val DEFAULT_PHYSICS_TIMESTEP: Double = 0.01
    }
}