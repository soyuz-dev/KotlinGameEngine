package org.soyuz.engine.entity

import org.soyuz.engine.shape.Shape2D
import org.soyuz.util.Transform
import org.soyuz.util.Vector2D

typealias GameEntityUpdateCallback = (entity: GameEntity, dt: Float) -> Unit

class DefaultGameEntity(
    override val id: String,
    override var transform: Transform = Transform(),
    override var shape: Shape2D? = null
) : GameEntity {
    private val updateCallbacks = mutableListOf<GameEntityUpdateCallback>()
    private val collisionCallbacks = mutableListOf<(other: GameEntity) -> Unit>()


    override fun onUpdate(callback: (dt: Float) -> Unit) {
        updateCallbacks.add { _, dt -> callback(dt) }
    }

    fun onUpdate(callback: GameEntityUpdateCallback) {
        updateCallbacks.add(callback)
    }

    override fun onCollision(callback: (other: GameEntity) -> Unit) {
        collisionCallbacks.add(callback)
    }

    override fun update(dt: Float) {
        updateCallbacks.forEach { callback ->
            callback(this, dt)
        }
    }

    override fun dispatchCollision(other: GameEntity) {
        collisionCallbacks.forEach { callback ->
            callback(other)
        }
    }

    fun goto(position: Vector2D) {
        this.transform = this.transform.goto(position)
    }
}
