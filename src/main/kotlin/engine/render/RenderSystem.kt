package org.soyuz.engine.render

import org.soyuz.engine.scene.Scene

interface RenderSystem {
    fun render(scene: Scene, camera: Camera, shader: Shader)
}