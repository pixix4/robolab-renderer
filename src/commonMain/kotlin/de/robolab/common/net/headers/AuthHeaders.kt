package de.robolab.common.net.headers

import de.robolab.common.net.BearerToken
import de.robolab.common.utils.decodeFromB64
import de.robolab.common.utils.encodeAsB64

sealed class AuthorizationHeader constructor(val schemaName: String, schemaValue: String) :
    Header(name, "$schemaName $schemaValue") {

    val schema: AuthenticationSchema?
        get() = AuthenticationSchema.parse(schemaName)

    constructor(schema: AuthenticationSchema, schemaValue: String) : this(schema.name, schemaValue)

    class Basic : AuthorizationHeader {
        val username: String
        val password: String

        constructor(encodedCredentials: String) : super(AuthenticationSchema.Basic.name, encodedCredentials) {
            val credentials = encodedCredentials.decodeFromB64(url = false).split(':', limit = 2)
            username = credentials[0]
            password = credentials[1]
        }

        constructor(username: String, password: String) : super(
            AuthenticationSchema.Basic,
            "$username:$password".encodeAsB64(url = false)
        ) {
            if (username.contains(':')) throw IllegalArgumentException("Username must not contain ':' (username: \"$username\")")
            this.username = username
            this.password = password
        }
    }

    class Bearer(val token: BearerToken) : AuthorizationHeader(AuthenticationSchema.Bearer, token.rawToken) {
        val rawToken: String = token.rawToken

        constructor(rawToken: String) : this(BearerToken(rawToken))
    }

    class Unknown(schemaName: String, schemaValue: String) : AuthorizationHeader(schemaName, schemaValue) {
        constructor(headerValue: String) : this(headerValue.substringBefore(' '), headerValue.substringAfter(' '))
    }


    companion object {
        const val name: String = "authorization"
    }
}

enum class AuthenticationSchema {
    Basic,
    Bearer
    ;

    companion object {

        private val lowerCaseValues: Map<String, AuthenticationSchema> = values().associateBy { it.name.toLowerCase() }

        fun parse(value: String): AuthenticationSchema? = lowerCaseValues[value.toLowerCase()]
    }
}