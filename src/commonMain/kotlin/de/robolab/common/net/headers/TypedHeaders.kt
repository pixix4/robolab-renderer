package de.robolab.common.net.headers

data class TypedHeaders(
    val contentTypeHeaders: List<ContentTypeHeader> = emptyList(),
    val lastModifiedHeader: LastModifiedHeader? = null
) {
    companion object {
        fun parseLowerCase(headers: Map<String, List<String>>): TypedHeaders {
            return TypedHeaders(
                contentTypeHeaders = headers[ContentTypeHeader.name]?.map(::ContentTypeHeader).orEmpty(),
                lastModifiedHeader = headers[LastModifiedHeader.name]?.map(::LastModifiedHeader)?.single()
            )
        }

        fun parse(headers: Map<String, List<String>>, simplifiedLowercase: Boolean = true): TypedHeaders =
            parseLowerCase(headers.toLowerCaseKeys(simplifiedLowercase))
    }
}

fun Map<String, List<String>>.toLowerCaseKeys(simplified: Boolean = false): Map<String, List<String>> {
    return if (simplified)
        this.mapKeys { it.key.toLowerCase() }
    else {
        val mutMap: MutableMap<String, List<String>> = mutableMapOf()
        for ((key: String, value: List<String>) in this) {
            val newKey = key.toLowerCase()
            val oldValue = mutMap.put(newKey, value)
            if (oldValue != null)
                mutMap[newKey] = oldValue + value
        }
        mutMap
    }
}
