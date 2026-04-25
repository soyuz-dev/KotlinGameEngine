package org.soyuz.engine.scene

import org.soyuz.engine.entity.GameEntity

open class RuntimeScene(
    final override val id: String
) : Scene {
    private val entities: LinkedHashMap<String, GameEntity> = linkedMapOf()

    final override fun addEntity(entity: GameEntity) {
        entities[entity.id] = entity
    }

    final override fun removeEntity(entityId: String) {
        entities.remove(entityId)
    }

    final override fun findEntity(entityId: String): GameEntity? = entities[entityId]

    final override fun allEntities(): List<GameEntity> = entities.values.toList()

    override fun init() = Unit

    override fun fixedUpdate(dt: Float) {
        entities.values.forEach { entity ->
            entity.update(dt)
        }
    }

    override fun render(delta: Float, alpha: Float) = Unit

    override fun cleanup() {
        entities.clear()
    }
}
