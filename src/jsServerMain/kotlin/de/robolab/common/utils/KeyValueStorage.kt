package de.robolab.common.utils

import de.robolab.server.externaljs.fs.existsSync
import de.robolab.server.externaljs.fs.readFileSync
import de.robolab.server.externaljs.fs.writeFileSync
import kotlinx.serialization.json.*

actual class KeyValueStorage {

    private val fileName = "server.json"

    private val fileCache: MutableMap<String, String> = mutableMapOf()
    private val json = Json(JsonConfiguration.Stable.copy(prettyPrint = true))

    private fun loadFile() {
        fun parseObject(prefix: String, json: JsonObject) {
            for ((key, value) in json) {
                if (value is JsonObject) {
                    parseObject("$prefix$key.", value.jsonObject)
                } else {
                    fileCache += "$prefix$key" to value.content
                }
            }
        }

        fileCache.clear()
        if (existsSync(fileName)) {
            val string = readFileSync(fileName, "utf8") as String

            val json = json.parseJson(string).jsonObject
            parseObject("", json)
        }
    }

    private fun saveFile() {
        val data = fileCache.mapKeys { (key, _) -> key.split('.') }.toList()

        fun JsonObjectBuilder.buildObject(depth: Int, data: List<Pair<List<String>, String>>) {
            val d = data.groupBy { it.first[depth] }

            for ((key, value) in d) {
                if (value.isEmpty()) continue

                val first = value.first()

                if (first.first.lastIndex == depth) {
                    key to first.second
                } else {
                    key to json {
                        buildObject(depth + 1, value)
                    }
                }
            }
        }


        val jsonObject = json {
            buildObject(0, data)
        }
        val string = json.stringify(JsonObjectSerializer, jsonObject)

        writeFileSync(fileName, string)
    }

    actual operator fun get(key: String): String? {
        return fileCache[key]
    }

    actual operator fun set(key: String, value: String?) {
        if (value == null) {
            fileCache -= key
        } else {
            fileCache[key] = value
        }
        saveFile()
    }

    actual operator fun contains(key: String): Boolean {
        return key in fileCache
    }

    actual fun clear() {
        fileCache.clear()
        saveFile()
    }

    actual fun keys(): Set<String> {
        return fileCache.keys
    }

    init {
        loadFile()
    }
}
