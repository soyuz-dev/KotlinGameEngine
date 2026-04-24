package org.soyuz.engine.core

import org.soyuz.engine.policy.EnginePolicy
import org.soyuz.engine.scene.Scene

interface Engine {
    val policy: EnginePolicy

    fun loadScene(scene: Scene)

    fun start()

    fun stop()

    fun tick(dt: Float)
}
