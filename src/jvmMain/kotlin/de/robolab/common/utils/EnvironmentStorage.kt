package de.robolab.common.utils

actual object EnvironmentStorage {

    actual operator fun get(key: String): String? {
        return System.getenv(key)
    }

    actual operator fun contains(key: String): Boolean {
        return System.getenv(key) != null
    }

    actual fun keys(): Set<String> {
        return System.getenv().keys
    }

}