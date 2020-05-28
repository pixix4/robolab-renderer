package de.robolab.common.utils

object IniConverter {

    private data class Key(
        val section: String?,
        val key: String
    ) {

        companion object {
            fun parse(key: String): Key {
                val split = key.split('.', limit = 2)

                if (split.size == 1) {
                    return Key(null, key)
                }
                return Key(split[0], split[1])
            }
        }
    }

    fun toString(data: Map<String, String>): String {
        val sectionMap = data.mapKeys { (k, _) ->
            Key.parse(k)
        }.toList().groupBy {
            it.first.section
        }

        val builder = StringBuilder()

        val globalSection = sectionMap[null]
        if (globalSection != null) {
            for ((key, value) in globalSection) {
                builder.append(key.key)
                builder.append('=')
                builder.append(value)
                builder.append('\n')
            }

            builder.append('\n')
        }


        for ((section, sectionContent) in sectionMap) {
            if (section == null) continue

            builder.append('[')
            builder.append(section)
            builder.append("]\n")

            for ((key, value) in sectionContent) {
                builder.append(key.key)
                builder.append('=')
                builder.append(value)
                builder.append('\n')
            }

            builder.append('\n')
        }

        return builder.toString()
    }

    fun fromString(content: String): Map<String, String> {
        var currentSection: String? = null
        val data = mutableMapOf<String, String>()

        val lines = content.splitToSequence('\n')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .filterNot { it.startsWith('#') }
            .filterNot { it.startsWith(';') }

        for (line in lines) {
            if (line.startsWith('[') && line.endsWith(']')) {
                currentSection = line.substring(1, line.lastIndex).trim()
            } else {
                val split = line.split('=', limit = 2)

                val key = split.first().trim()
                val value = split.getOrNull(1)?.trim() ?: ""

                val sectionKey = currentSection?.let { "$it.$key" } ?: key

                data[sectionKey] = value
            }
        }

        return data
    }
}
