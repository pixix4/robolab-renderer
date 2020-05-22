package de.robolab.common.utils

actual class KeyValueStorage actual constructor() {
    actual operator fun get(key: String): String? {
        throw UnsupportedOperationException()
    }

    actual operator fun set(key: String, value: String?) {
        throw UnsupportedOperationException()
    }

    actual operator fun contains(key: String): Boolean {
        throw UnsupportedOperationException()
    }

    actual fun clear() {
        throw UnsupportedOperationException()
    }

}