package org.soyuz.util

object Debug {
    @Volatile var enabled: Boolean = false

    inline fun log(message: () -> String) {
        if (enabled) System.err.println(message())
    }

    inline fun <T> tap(value: T, message: (T) -> String = { it.toString() }): T {
        if (enabled) System.err.println(message(value))
        return value
    }
}