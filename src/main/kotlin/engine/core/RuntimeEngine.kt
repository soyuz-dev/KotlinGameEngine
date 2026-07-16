package org.soyuz.engine.core

import org.lwjgl.glfw.GLFW.glfwGetTime
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
import org.soyuz.windowing.Window

class RuntimeEngine(
    val window: Window,
    private val physicsSystem: PhysicsSystem?,
    private val camera: Camera
) : Engine, Dynamic {

    lateinit var renderSystem: RenderSystem
    lateinit var shader: Shader

    var width: Int
        get() = window.width
        set(value) {window.width = value }
    var height: Int
        get() = window.height
        set(value) {window.height = value }
    var title: String
        get() = window.title
        set(value) {window.title = value }

    private var currentScene: Scene? = null
    private var running = false
    private val timers = mutableListOf<Timer>()

    private data class Timer(
        val intervalMs: Double,
        var accumulator: Double = 0.0,
        val type: TimerType = TimerType.INTERVAL,
        val callback: (progress: Double) -> Unit
    )

    private enum class TimerType { INTERVAL, ONE_SHOT, DURING, FOREVER }

    fun init() {
        window.makeContextCurrent()
        window.setCharCallback { onChar(it) }
        camera.setOrtho(window.width.toFloat(), window.height.toFloat())
        AudioSystem.init()
    }

    fun onChar(char: Char) {
        val scene = currentScene ?: return
        UISystem.handleChar(char, scene.allEntities())
    }

    fun forEvery(ms: Double, callback: () -> Unit) {
        timers.add(Timer(intervalMs = ms, type = TimerType.INTERVAL) { callback() })
    }

    fun everyFrame(callback: (dt: Double) -> Unit) {
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
        if (running) scene.init()
    }

    override fun start() {
        if (running) return
        if (!::renderSystem.isInitialized || !::shader.isInitialized) {
            throw IllegalStateException("renderSystem and shader must be assigned before starting the engine.")
        }
        running = true
        currentScene?.init()
    }

    override fun stop() {
        if (!running) return
        running = false
        currentScene?.cleanup()
        AudioSystem.cleanup()
        Assets.cleanup()
    }

    private var physicsAccumulator = 0.0

    override fun update(dt: Float) {
        if (!running || dt <= 0f || !dt.isFinite()) return
        val scene = currentScene ?: return

        physicsAccumulator += dt.toDouble()
        while (physicsAccumulator >= DEFAULT_PHYSICS_TIMESTEP) {
            physicsSystem?.step(scene, DEFAULT_PHYSICS_TIMESTEP)
            physicsAccumulator -= DEFAULT_PHYSICS_TIMESTEP
        }

        val entities = scene.allEntities()
        UISystem.update(entities)
        entities.forEach { (it.painter as? Dynamic)?.update(dt) }

        val dtMs = dt * 1000.0
        timers.removeAll { timer ->
            timer.accumulator += dtMs
            when (timer.type) {
                TimerType.INTERVAL -> {
                    while (timer.accumulator >= timer.intervalMs) { timer.accumulator -= timer.intervalMs; timer.callback(0.0) }
                    false
                }
                TimerType.ONE_SHOT -> { if (timer.accumulator >= timer.intervalMs) { timer.callback(1.0); true } else false }
                TimerType.DURING -> {
                    if (timer.accumulator < timer.intervalMs) { timer.callback((timer.accumulator / timer.intervalMs).coerceIn(0.0, 1.0)); false }
                    else { timer.callback(1.0); true }
                }
                TimerType.FOREVER -> { timer.callback(dt.toDouble()); false }
            }
        }
    }

    fun render() {
        if (!running) return
        val scene = currentScene ?: return
        window.makeContextCurrent()
        renderSystem.render(scene, camera, shader)
    }

    override fun run() {
        start()
        var lastTime = glfwGetTime()
        while (!window.shouldClose()) {
            val currentTime = glfwGetTime()
            val dt = (currentTime - lastTime).toFloat().coerceIn(0f, DEFAULT_MAX_FRAME_DELTA.toFloat())
            lastTime = currentTime
            window.pollEvents()
            update(dt)
            render()
            window.swapBuffers()
            KeyListener.endFrame()
            MouseListener.endFrame()
        }
        stop()
        window.destroy()
    }

    // Window delegation
    fun quit() = window.quit()
    fun minimize() = window.minimize()
    fun restore() = window.restore()
    fun maximize() = window.maximize()
    fun isMinimized() = window.isMinimized()
    fun isMaximized() = window.isMaximized()
    fun isFocused() = window.isFocused()
    fun center() = window.center()
    fun setFullscreen(fullscreen: Boolean) = window.setFullscreen(fullscreen)
    fun setVSync(enabled: Boolean) = window.setVSync(enabled)

    override fun invoke(callback: (dt: Double) -> Unit) = everyFrame(callback)

    companion object {
        const val DEFAULT_MAX_FRAME_DELTA: Double = 0.25
        const val DEFAULT_PHYSICS_TIMESTEP: Double = 0.01
    }
}