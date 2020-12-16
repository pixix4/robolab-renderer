package de.robolab.common.utils

expect object EnvironmentStorage {

    operator fun get(key: String): String?

    operator fun contains(key: String): Boolean

    fun keys(): Set<String>
}
