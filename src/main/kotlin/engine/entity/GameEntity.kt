package org.soyuz.engine.entity

import org.soyuz.engine.render.Painter
import org.soyuz.engine.shape.Shape2D
import org.soyuz.util.Transform

interface GameEntity {
    val id: String

    var transform: Transform

    var shape: Shape2D?

    var painter : Painter?

    fun onUpdate(callback: (dt: Float) -> Unit)

    fun onCollision(callback: (other: GameEntity) -> Unit)

    fun update(dt: Float)

    fun dispatchCollision(other: GameEntity)
}
