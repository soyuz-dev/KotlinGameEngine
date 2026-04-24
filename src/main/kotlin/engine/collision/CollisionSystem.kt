package org.soyuz.engine.collision

import org.soyuz.engine.scene.Scene

interface CollisionSystem {
    fun registerCollider(entityId: String, collider: Collider)

    fun unregisterCollider(entityId: String)

    fun detect(scene: Scene)
}
