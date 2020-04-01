package de.robolab.utils

expect class KeyValueStorage() {
    operator fun get(key: String): String?

    operator fun set(key: String, value: String?)

    operator fun contains(key: String): Boolean

    fun clear()
}
