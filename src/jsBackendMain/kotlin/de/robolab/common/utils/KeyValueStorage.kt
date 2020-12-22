package de.robolab.common.utils

import de.robolab.server.externaljs.fs.existsSync
import de.robolab.server.externaljs.fs.readFileSync
import de.robolab.server.externaljs.fs.writeFileSync

actual class KeyValueStorage {

    private val debugAccess: Boolean

    private val fileName = "server.ini"

    private val log = Logger("KeyValueStorage")

    private var configCache: Map<String, String> = emptyMap()

    private fun loadFile(filename: String) {
        if (existsSync(filename)) {
            println("Loading config file '$filename'!")
            val string = readFileSync(filename, "utf8") as String

            configCache += IniConverter.fromString(string)
        } else {
            println("Config file '$filename' does not exist!")
        }
    }

    private fun saveFile() {
        val string = IniConverter.toString(configCache)

        writeFileSync(fileName, string)
    }

    actual operator fun get(key: String): String? {
        if (debugAccess) {
            val value = configCache[key]
            println("Read value $key: $value")
            return value
        }
        return configCache[key]
    }

    actual operator fun set(key: String, value: String?) {
        if (value == null) {
            if (debugAccess) {
                println("Remove key $key: (${configCache[key]})")
            }
            configCache -= key
        } else {
            if (debugAccess) {
                println("Set value $key: ${configCache[key]} -> $value")
            }
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
        try {
            loadFile(fileName)
        } catch (_: Exception) {
            log.error("Cannot load config file $fileName")
        }

        val files = overrideFiles
        if (files == null) {
            debugAccess = true
            println(
                """
                |-------------------------------------------------------------------|
                |                                                                   |
                | KeyValueStorage was initialized before the command line arguments |
                |   could be processed. External config files will not be loaded!   |
                |                                                                   |
                |-------------------------------------------------------------------|
                """.trimIndent()
            )
        } else {
            debugAccess = false
            for (file in files) {
                try {
                    loadFile(file)
                } catch (_: Exception) {
                    log.error("Cannot load config file $file")
                }
            }
        }
    }

    companion object {
        var overrideFiles: List<String>? = null
    }
}
