package org.soyuz.engine.physics

import org.soyuz.engine.physics.forcefields.EntityAwareForceField
import org.soyuz.engine.physics.joints.Joint
import org.soyuz.engine.scene.Scene

interface PhysicsSystem {

    fun getBody(entityId: String): PhysicsBody?

    fun registerBody(entityId: String, body: PhysicsBody)

    fun unregisterBody(entityId: String)

    fun step(scene: Scene, dt: Double)

    fun addDynamicField(field: EntityAwareForceField)

    fun removeDynamicField(field: EntityAwareForceField)

    fun addJoint(joint: Joint)

    fun removeJoint(joint: Joint)
}
