package org.soyuz.engine.core

import org.soyuz.engine.scene.Scene

interface Engine {
    fun loadScene(scene: Scene)
    fun start()
    fun stop()
}