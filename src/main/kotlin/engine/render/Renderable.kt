package org.soyuz.engine.render

import org.soyuz.engine.entity.Transform

interface Renderable {
    fun render(transform: Transform)

    fun cleanup()
}
