package org.soyuz.engine.physics.forcefields

import org.soyuz.engine.physics.PhysicsBody
import org.soyuz.util.math.Vector2D

interface EntityAwareForceField: ForceField {
    fun registerBody(entityId:String, body: PhysicsBody, position: Vector2D)
    fun unregisterBody(entityId:String)
    fun updatePosition(entityId:String, position: Vector2D)
}