package org.soyuz.engine.physics

import org.soyuz.engine.physics.forcefields.DynamicForceField
import org.soyuz.engine.scene.Scene

interface PhysicsSystem {
    fun registerBody(entityId: String, body: PhysicsBody)

    fun unregisterBody(entityId: String)

    fun step(scene: Scene, dt: Double)

    fun addDynamicField(field: DynamicForceField)
}
