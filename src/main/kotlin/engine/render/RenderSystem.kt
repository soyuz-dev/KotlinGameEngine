package org.soyuz.engine.render

import org.soyuz.engine.scene.Scene
import org.soyuz.windowing.Window

interface RenderSystem {
    fun render(window: Window, scene: Scene, camera: Camera, shader: Shader)

    fun cleanup() = Unit
}
