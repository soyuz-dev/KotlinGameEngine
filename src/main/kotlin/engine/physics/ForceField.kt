package org.soyuz.engine.physics

import org.soyuz.engine.entity.GameEntity
import org.soyuz.util.Vector2D

interface ForceField {
    fun forceFor(entity: GameEntity): Vector2D
}
