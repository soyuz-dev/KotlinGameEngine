package org.soyuz.engine.entity

import org.soyuz.engine.shape.Shape2D

interface GameEntity {
    val id: String

    var shape: Shape2D?

    fun onUpdate(callback: (dt: Float) -> Unit)

    fun onCollision(callback: (other: GameEntity) -> Unit)

    fun update(dt: Float)

    fun dispatchCollision(other: GameEntity)
}
