package org.soyuz.engine.entity

import org.soyuz.input.Input

/**
 * Marker for entities that consume scene-managed input.
 */
interface InputAwareGameEntity {
    fun bindInput(input: Input)
}
