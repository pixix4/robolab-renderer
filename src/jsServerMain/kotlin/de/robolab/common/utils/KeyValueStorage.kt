package de.robolab.common.utils

import de.robolab.server.externaljs.fs.existsSync
import de.robolab.server.externaljs.fs.readFileSync
import de.robolab.server.externaljs.fs.writeFileSync

actual class KeyValueStorage {

    private val fileName = "server.ini"

    private var configCache: Map<String, String> = emptyMap()

    private fun loadFile(filename: String) {
        if (existsSync(filename)) {
            val string = readFileSync(filename, "utf8") as String

            configCache += IniConverter.fromString(string)
        }
    }

    private fun saveFile() {
        val string = IniConverter.toString(configCache)

        writeFileSync(fileName, string)
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
        val log = Logger(this)
        try {
            loadFile(fileName)
        } catch (_: Exception) {
            log.error("Cannot load config file $fileName")
        }

        for (file in overrideFiles) {
            try {
                loadFile(file)
            } catch (_: Exception) {
                log.error("Cannot load config file $file")
            }
        }
    }

    companion object {
        var overrideFiles = emptyList<String>()
    }
}
