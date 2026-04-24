package org.soyuz.engine.scene

import org.soyuz.engine.entity.GameEntity

interface Scene {
    val id: String

    fun addEntity(entity: GameEntity)

    fun removeEntity(entityId: String)

    fun findEntity(entityId: String): GameEntity?

    fun allEntities(): List<GameEntity>

    fun update(dt: Float)
}
