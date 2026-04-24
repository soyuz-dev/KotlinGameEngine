package org.soyuz.engine.render

import org.soyuz.engine.scene.Scene

interface RenderSystem {
    fun registerRenderable(entityId: String, renderable: Renderable)

    fun unregisterRenderable(entityId: String)

    fun render(scene: Scene)
}
