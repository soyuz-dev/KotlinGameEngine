package org.soyuz.engine.physics.joints

import org.soyuz.util.Vector2D

interface PermissiveJoint : Joint {
    fun accumulateForces(positionA: Vector2D, positionB: Vector2D)
}