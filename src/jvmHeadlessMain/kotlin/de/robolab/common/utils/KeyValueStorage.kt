package de.robolab.common.utils

@Suppress("SuspiciousCollectionReassignment")
actual class KeyValueStorage {

    private var configCache: Map<String, String> = emptyMap()

    private fun loadFile() {
        configCache = IniConverter.fromString(ConfigFile.readSystemConfig())
    }

    private fun saveFile() {
        ConfigFile.writeSystemConfig(IniConverter.toString(configCache))
    }

    actual operator fun get(key: String): String? {
        return configCache[key]
    }

    actual operator fun set(key: String, value: String?) {
        if (value == null) {
            configCache -= key
        } else {
            configCache += key to value
        }
        saveFile()
    }

    actual operator fun contains(key: String): Boolean {
        return key in configCache
    }

    actual fun clear() {
        configCache = emptyMap()
        saveFile()
    }

    actual fun keys(): Set<String> {
        return configCache.keys
    }

    init {
        loadFile()
    }
}
