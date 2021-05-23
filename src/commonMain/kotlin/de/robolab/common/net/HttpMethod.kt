package de.robolab.common.net

enum class HttpMethod {
    HEAD,
    GET,
    POST,
    PUT,
    DELETE;

    companion object {

        private val nameMapping: Map<String, HttpMethod> = HttpMethod.values().associateBy { it.name.uppercase() }

        fun parse(name: String): HttpMethod? {
            return nameMapping[name.uppercase()]
        }
    }
}