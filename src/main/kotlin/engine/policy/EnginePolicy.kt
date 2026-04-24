package org.soyuz.engine.policy

data class EnginePolicy(
    val collisionPolicy: CollisionPolicy,
    val physicsPolicy: PhysicsPolicy
)
