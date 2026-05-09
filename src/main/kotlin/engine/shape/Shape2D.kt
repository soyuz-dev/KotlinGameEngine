package org.soyuz.engine.shape

sealed interface Shape2D {
    fun localAabb(): Aabb2D
    fun supportRadius(): Double
}
