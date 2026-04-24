package org.soyuz.entity

import org.soyuz.input.Input

typealias UpdateFunction = (entity: Entity, input: Input, dt: Float) -> Unit

class Entity {
    private val updateCallbacks = mutableListOf<UpdateFunction>()

    fun onUpdate(callback: UpdateFunction) {
        updateCallbacks.add(callback)
    }

    fun update(input: Input, dt: Float) {
        for (cb in updateCallbacks) {
            cb(this, input, dt)
        }
    }
}