package org.soyuz.engine.physics.joints

import org.soyuz.util.math.Vector2D

interface PermissiveJoint : Joint {
    fun accumulateForces(positionA: Vector2D, positionB: Vector2D)
}