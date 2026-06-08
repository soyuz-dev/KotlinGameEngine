package org.soyuz.engine.physics.joints

import org.soyuz.util.Vector2D

interface StrictJoint : Joint {
    fun solvePositions(positionA: Vector2D, positionB: Vector2D, dt: Double): Pair<Vector2D, Vector2D>
}