package org.soyuz.engine.collision

import org.soyuz.engine.scene.Scene
import org.soyuz.util.Debug

class RuntimeCollisionSystem : CollisionSystem {

    private val colliders = mutableMapOf<String, Collider>()

    override fun registerCollider(entityId: String, collider: Collider) {
        require(entityId !in colliders) {"Entity '${entityId}' already has a registered collider."}
        require(colliders.values.none {it === collider}) {"Collider already registered to another entity. Create a new collider instead."}
        colliders[entityId] = collider
    }

    override fun unregisterCollider(entityId: String) {
        colliders.remove(entityId)
    }

    override fun detect(scene: Scene) {
        val entities = scene.allEntities().filter { it.id in colliders }

        for (i in entities.indices) {
            for (j in i+1 until entities.size) {
                val a = entities[i]
                val b = entities[j]
                val colliderA = colliders[a.id]!!
                val colliderB = colliders[b.id]!!

                if (colliderA.intersects(colliderB, a.transform, b.transform)) {
                    //TODO: Produce Contact, fire events
                    Debug.log {"Collision: ${a.id} hit ${b.id}"}
                }
            }
        }
    }
}