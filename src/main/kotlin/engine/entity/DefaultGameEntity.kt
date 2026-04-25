package org.soyuz.engine.entity

import org.soyuz.engine.shape.Shape2D
import org.soyuz.input.Input

typealias GameEntityUpdateCallback = (entity: GameEntity, input: Input, dt: Float) -> Unit

class DefaultGameEntity(
    final override val id: String,
    final override var shape: Shape2D? = null
) : GameEntity, InputAwareGameEntity {
    private val updateCallbacks = mutableListOf<GameEntityUpdateCallback>()
    private val collisionCallbacks = mutableListOf<(other: GameEntity) -> Unit>()

    private var input: Input = Input()

    override fun onUpdate(callback: (dt: Float) -> Unit) {
        updateCallbacks.add { _, _, dt -> callback(dt) }
    }

    fun onUpdate(callback: GameEntityUpdateCallback) {
        updateCallbacks.add(callback)
    }

    override fun onCollision(callback: (other: GameEntity) -> Unit) {
        collisionCallbacks.add(callback)
    }

    override fun update(dt: Float) {
        updateCallbacks.forEach { callback ->
            callback(this, input, dt)
        }
    }

    override fun dispatchCollision(other: GameEntity) {
        collisionCallbacks.forEach { callback ->
            callback(other)
        }
    }

    override fun bindInput(input: Input) {
        this.input = input
    }
}
