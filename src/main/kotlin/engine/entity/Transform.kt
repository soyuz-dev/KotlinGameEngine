package org.soyuz.engine.entity

import org.soyuz.util.Vector2D

interface Transform {
    var position: Vector2D

    var rotationRadians: Double

    var scale: Vector2D

    fun translate(delta: Vector2D)

    fun rotate(deltaRadians: Double)
}
