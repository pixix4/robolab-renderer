package de.robolab.common.utils

actual object EnvironmentStorage {

    private val storage: Map<String, String>

    actual operator fun get(key: String): String? {
        return storage[key]
    }

    actual operator fun contains(key: String): Boolean {
        return key in storage
    }

    actual fun keys(): Set<String> {
        return storage.keys
    }

    init {
        val keyArray = Object.keys(process.env) as Array<String>
        storage = keyArray.map {
            it to process.env[it] as String
        }.toMap()
    }
}

external val Object: dynamic = definedExternally
external val process: dynamic = definedExternally
