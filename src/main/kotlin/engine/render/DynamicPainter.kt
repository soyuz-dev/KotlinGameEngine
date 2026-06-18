package org.soyuz.engine.render

interface DynamicPainter : Painter {
    fun update(dt:Double)
}