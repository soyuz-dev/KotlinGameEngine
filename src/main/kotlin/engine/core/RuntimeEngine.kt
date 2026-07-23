package org.soyuz.engine.core

import org.soyuz.engine.physics.PhysicsSystem
import org.soyuz.engine.render.Camera
import org.soyuz.engine.render.RenderSystem
import org.soyuz.engine.render.Shader
import org.soyuz.engine.scene.Scene
import org.soyuz.engine.ui.UISystem
import org.soyuz.engine.ui.UI
import org.soyuz.input.Input
import org.soyuz.util.Dynamic
import org.soyuz.windowing.Window

class RuntimeEngine(
    val window: Window,
    private val physicsSystem: PhysicsSystem?,
    private val camera: Camera,
) : Engine, Dynamic {

    val input = Input(window.handle)
    private val uiSystem = UISystem(input)
    val ui = UI(uiSystem)
    private val pendingTimers = mutableListOf<Timer>()

    lateinit var renderSystem: RenderSystem
    lateinit var shader: Shader

    private var currentScene: Scene? = null
    private var running = false
    private var renderResourcesCleaned = false
    private val timers = mutableListOf<Timer>()

    private data class Timer(
        val intervalMs: Double,
        var accumulator: Double = 0.0,
        val type: TimerType = TimerType.INTERVAL,
        val callback: (progress: Double) -> Unit
    )

    private enum class TimerType { INTERVAL, ONE_SHOT, DURING, FOREVER }

    fun init() {
        window.setCharCallback { onChar(it) }
        camera.setOrtho(window.width.toFloat(), window.height.toFloat())
    }

    fun onChar(char: Char) {
        val scene = currentScene ?: return
        uiSystem.handleChar(char, scene.allEntities())
    }

    fun forEvery(ms: Double, callback: () -> Unit) {
        pendingTimers.add(Timer(intervalMs = ms, type = TimerType.INTERVAL) { callback() })
    }

    fun everyFrame(callback: (dt: Double) -> Unit) {
        timers.add(Timer(intervalMs = 0.0, type = TimerType.FOREVER) { callback(it) })
    }

    fun after(ms: Double, callback: () -> Unit) {
        pendingTimers.add(Timer(intervalMs = ms, type = TimerType.ONE_SHOT) { callback() })
    }

    fun during(ms: Double, callback: (progress: Double) -> Unit) {
        pendingTimers.add(Timer(intervalMs = ms, type = TimerType.DURING, callback = callback))
    }

    override fun loadScene(scene: Scene) {
        currentScene?.cleanup()
        currentScene = scene
        if (running) scene.init()
    }

    override fun start() {
        if (running) return
        check(!renderResourcesCleaned) { "Cannot restart an engine after its render resources have been cleaned." }
        if (!::renderSystem.isInitialized || !::shader.isInitialized) {
            throw IllegalStateException("renderSystem and shader must be assigned before starting the engine.")
        }
        running = true
        currentScene?.init()
    }

    override fun stop() {
        if (running) {
            running = false
            currentScene?.cleanup()
        }
        if (!renderResourcesCleaned && ::renderSystem.isInitialized) {
            renderSystem.cleanup()
            renderResourcesCleaned = true
        }
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
        uiSystem.update(entities)
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

        timers.addAll(pendingTimers)
        pendingTimers.clear()
    }

    fun render() {
        window.makeContextCurrent()
        renderCurrentContext()
    }

    fun renderCurrentContext() {
        if (!running) return
        val scene = currentScene ?: return
        renderSystem.render(window, scene, camera, shader)
    }

    override fun run() {
        throw UnsupportedOperationException("Use Application and WindowManager to run RuntimeEngine instances.")
    }

    // Window delegation
    fun quit() = window.quit()


    override fun invoke(callback: (dt: Double) -> Unit) = everyFrame(callback)

    companion object {
        const val DEFAULT_MAX_FRAME_DELTA: Double = 0.25
        const val DEFAULT_PHYSICS_TIMESTEP: Double = 0.01
    }
}
