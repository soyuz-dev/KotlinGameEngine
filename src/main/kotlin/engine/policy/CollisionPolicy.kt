package org.soyuz.engine.policy

data class CollisionPolicy(
    val eventsEnabled: Boolean,
    val maxListenersPerEntity: Int
)
