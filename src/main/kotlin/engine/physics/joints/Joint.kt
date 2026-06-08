package org.soyuz.engine.physics.joints

import org.soyuz.engine.physics.PhysicsBody

sealed interface Joint {
    val bodyA: PhysicsBody
    val bodyB: PhysicsBody
}