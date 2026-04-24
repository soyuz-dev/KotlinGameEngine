package org.soyuz.engine.events

data class CollisionEvent(
    val sourceEntityId: String,
    val otherEntityId: String
)
