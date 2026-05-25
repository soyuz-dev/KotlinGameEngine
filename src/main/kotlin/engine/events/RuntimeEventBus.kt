package org.soyuz.engine.events

class RuntimeEventBus : EventBus {

    private val listeners = mutableMapOf<Class<*>, MutableList<(Any) -> Unit>>()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> subscribe(eventType: Class<T>, listener: (T) -> Unit) {
        listeners.getOrPut(eventType) { mutableListOf() } .add(listener as (Any) -> Unit)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> unsubscribe(eventType: Class<T>, listener: (T) -> Unit) {
        listeners[eventType]?.remove(listener as (Any) -> Unit)
    }

    override fun publish(event: Any) {
        listeners[event.javaClass]?.forEach {it(event)}
    }


}