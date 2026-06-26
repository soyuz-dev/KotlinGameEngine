package org.soyuz.engine.render


class CompositePainter(
    private val layers: List<Painter>
) : Painter {
    constructor(vararg layers: Painter) : this(layers.toList())

    override fun bind(shader: Shader) {
        layers.forEach { it.bind(shader) }
    }
}