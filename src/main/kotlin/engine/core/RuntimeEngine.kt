package org.soyuz.engine.core

import org.soyuz.engine.policy.EnginePolicy
import org.soyuz.engine.scene.Scene

class RuntimeEngine(
    override val policy: EnginePolicy,
    private val fixedTimeStep: Float = DEFAULT_FIXED_TIME_STEP,
    private val maxFrameDelta: Float = DEFAULT_MAX_FRAME_DELTA
) : Engine {
    init {
        require(fixedTimeStep > 0f) { "fixedTimeStep must be > 0" }
        require(maxFrameDelta >= 0f) { "maxFrameDelta must be >= 0" }
    }

    private var running: Boolean = false
    private var currentScene: Scene? = null
    private var accumulator: Float = 0f

    override fun loadScene(scene: Scene) {
        if (scene === currentScene) {
            return
        }

        currentScene?.cleanup()
        currentScene = scene
        accumulator = 0f

        if (running) {
            scene.init()
        }
    }

    override fun start() {
        if (running) {
            return
        }

        running = true
        accumulator = 0f
        currentScene?.init()
    }

    override fun stop() {
        if (!running) {
            return
        }

        running = false
        accumulator = 0f
    }

    override fun tick(dt: Float) {
        if (!running) {
            return
        }

        val scene = currentScene ?: return
        val frameDelta = dt.coerceIn(0f, maxFrameDelta)

        accumulator += frameDelta
        while (accumulator >= fixedTimeStep) {
            scene.fixedUpdate(fixedTimeStep)
            accumulator -= fixedTimeStep
        }

        val alpha = (accumulator / fixedTimeStep).coerceIn(0f, 1f)
        scene.render(frameDelta, alpha)
    }

    companion object {
        const val DEFAULT_FIXED_TIME_STEP: Float = 1f / 60f
        const val DEFAULT_MAX_FRAME_DELTA: Float = 0.25f
    }
}
