package org.soyuz.engine.events

data class CollisionEvent(
    val sourceEntityId: String,
    val otherEntityId: String,
    val type: CollisionEventType = CollisionEventType.ENTER
)

enum class CollisionEventType { ENTER, EXIT, STAY }
