package org.soyuz.engine.core

import org.soyuz.engine.physics.PhysicsSystem
import org.soyuz.engine.render.Camera
import org.soyuz.engine.render.RenderSystem
import org.soyuz.engine.render.Shader
import org.soyuz.engine.scene.Scene
import org.soyuz.engine.ui.UISystem
import org.soyuz.util.Dynamic

class RuntimeEngine(
    private val physicsSystem: PhysicsSystem?,
    private val renderSystem: RenderSystem,
    private val shader: Shader,
    private val camera: Camera
) : Engine, Dynamic {

    private var running: Boolean = false
    private var currentScene: Scene? = null
    private var accumulator: Double = 0.0

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
        running = true
        accumulator = 0.0
        currentScene?.init()
    }

    override fun stop() {
        if (!running) return
        running = false
        currentScene?.cleanup()
    }

    override fun update(dt: Float) {
        if (!running) return
        val scene = currentScene ?: return
        if (dt <= 0f || !dt.isFinite()) return

        val dtDouble = dt.toDouble()

        // UI input routing
        UISystem.update(scene.allEntities())

        // Physics step
        physicsSystem?.step(scene, dtDouble)

        // Update dynamic painters
        scene.allEntities().forEach { entity ->
            (entity.painter as? Dynamic)?.update(dt)
        }
    }

    fun render() {
        val scene = currentScene ?: return
        if (!running) return
        renderSystem.render(scene, camera, shader)
    }

    companion object {
        const val DEFAULT_MAX_FRAME_DELTA: Double = 0.25
    }
}