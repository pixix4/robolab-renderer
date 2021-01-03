package de.robolab.common.utils

actual object EnvironmentStorage {
    actual operator fun get(key: String): String? {
        return null
    }

    actual operator fun contains(key: String): Boolean {
        return false
    }

    actual fun keys(): Set<String> {
        return emptySet()
    }

}