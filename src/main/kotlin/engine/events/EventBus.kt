package org.soyuz.engine.events

interface EventBus {
    fun <T : Any> subscribe(eventType: Class<T>, listener: (T) -> Unit)

    fun <T : Any> unsubscribe(eventType: Class<T>, listener: (T) -> Unit)

    fun publish(event: Any)
}
