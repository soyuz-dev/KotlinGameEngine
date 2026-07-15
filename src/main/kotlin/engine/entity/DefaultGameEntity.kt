package org.soyuz.engine.entity

import org.soyuz.engine.collision.Collider
import org.soyuz.engine.render.Painter
import org.soyuz.engine.shape.Shape2D
import org.soyuz.engine.ui.Interactive
import org.soyuz.util.math.Transform
import org.soyuz.util.math.Vector2D

typealias GameEntityUpdateCallback = (entity: GameEntity, dt: Float) -> Unit

open class DefaultGameEntity(
    override val id: String,
    override var transform: Transform = Transform(),
    override var painter: Painter? = null,
    shape: Shape2D? = null,
    override var collider: Collider? = null,
    override var interactive: Interactive? = null,
) : GameEntity {


    override var shape: Shape2D? = null
        set(value) {
            field = value
            shapeListeners.forEach {
                it(value)
            }
        }


    private val updateCallbacks = mutableListOf<GameEntityUpdateCallback>()
    private val collisionCallbacks = mutableListOf<(other: GameEntity) -> Unit>()

    var position: Vector2D
        get() = transform.position
        set(value) {
            transform = transform.copy(position = value)
            positionListeners.forEach { it(value) }
        }

    var rotation: Double
        get() = transform.rotationRadians
        set(value) {
            transform = transform.copy(rotationRadians = value)
            rotationListeners.forEach { it(value) }
        }


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

    fun turnTo(rotationRadians: Double) {
        this.transform = this.transform.copy(rotationRadians = rotationRadians)
    }

    private val positionListeners = mutableListOf<(Vector2D) -> Unit>()
    private val rotationListeners = mutableListOf<(Double) -> Unit>()

    private val shapeListeners = mutableListOf<(Shape2D?) -> Unit>()

    fun onPositionChanged(callback: (Vector2D) -> Unit) {
        positionListeners.add(callback)
        callback(position) // fire immediately with current value
    }

    fun onRotationChanged(callback: (Double) -> Unit) {
        rotationListeners.add(callback)
        callback(rotation)
    }

    fun onShapeChanged(callback: (Shape2D?) -> Unit) {
        shapeListeners.add(callback)
        callback(shape)
    }


    init {
        this.shape = shape
    }
}
