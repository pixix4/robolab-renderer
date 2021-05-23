package de.robolab.common.net.headers

data class TypedHeaders(
    val contentTypeHeaders: List<ContentTypeHeader> = emptyList(),
    val lastModifiedHeader: LastModifiedHeader? = null
) {
    companion object {
        fun parseLowerCase(headers: Map<String, List<String>>): TypedHeaders {
            return TypedHeaders(
                contentTypeHeaders = headers.filterKeys {
                    it.equals(ContentTypeHeader.name, true)
                }.values.flatten().map(::ContentTypeHeader),
                lastModifiedHeader = headers.filterKeys {
                    it.equals(LastModifiedHeader.name, true)
                }.values.flatten().map(::LastModifiedHeader).singleOrNull()
            )
        }

        fun parse(headers: Map<String, List<String>>, simplifiedLowercase: Boolean = true): TypedHeaders =
            parseLowerCase(headers.toLowerCaseKeys(simplifiedLowercase))
    }
}

fun Map<String, List<String>>.toLowerCaseKeys(simplified: Boolean = false): Map<String, List<String>> {
    return if (simplified)
        this.mapKeys { it.key.lowercase() }
    else {
        val mutMap: MutableMap<String, List<String>> = mutableMapOf()
        for ((key: String, value: List<String>) in this) {
            val newKey = key.lowercase()
            val oldValue = mutMap.put(newKey, value)
            if (oldValue != null)
                mutMap[newKey] = oldValue + value
        }
        mutMap
    }
}
