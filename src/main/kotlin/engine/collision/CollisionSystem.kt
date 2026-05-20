package org.soyuz.engine.collision

import org.soyuz.engine.scene.Scene

interface CollisionSystem {
    fun hasCollider(entityId: String): Boolean

    fun getCollider(entityId: String): Collider?

    fun registerCollider(entityId: String, collider: Collider)

    fun unregisterCollider(entityId: String)

    fun detect(scene: Scene) : List<Contact>
}
