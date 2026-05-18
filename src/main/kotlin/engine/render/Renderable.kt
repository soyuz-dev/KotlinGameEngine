package org.soyuz.engine.render

import org.soyuz.util.Transform

interface Renderable {
    fun render(transform: Transform)

    fun cleanup()
}
