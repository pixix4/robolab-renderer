package de.robolab.common.net.headers

import de.robolab.client.net.ICredentialProvider
import de.robolab.common.utils.decodeFromB64
import de.robolab.common.utils.encodeAsB64
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = AuthorizationHeader.AuthorizationHeaderSerializer::class)
sealed class AuthorizationHeader constructor(val schemaName: String, val schemaValue: String) :
    Header(name, "$schemaName $schemaValue") {

    val schema: AuthenticationSchema?
        get() = AuthenticationSchema.parse(schemaName)

    constructor(schema: AuthenticationSchema, schemaValue: String) : this(schema.name, schemaValue)

    class Basic : AuthorizationHeader, ICredentialProvider {
        override val username: String
        override val password: String

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

    class Bearer(val token: String) : AuthorizationHeader(AuthenticationSchema.Bearer, token)

    class Unknown(schemaName: String, schemaValue: String) : AuthorizationHeader(schemaName, schemaValue) {
        constructor(headerValue: String) : this(headerValue.substringBefore(' '), headerValue.substringAfter(' '))
    }

    override fun toString() = schemaName + " " + schemaValue

    companion object {
        const val name: String = "authorization"

        fun parse(value: String): AuthorizationHeader {
            val (schemaName: String, schemaValue: String) = value.split(' ', limit = 2)
            return when (AuthenticationSchema.parse(schemaName)) {
                null -> Unknown(schemaName, schemaValue)
                AuthenticationSchema.Basic -> Basic(schemaValue)
                AuthenticationSchema.Bearer -> Bearer(schemaValue)
            }
        }
    }

    object AuthorizationHeaderSerializer : KSerializer<AuthorizationHeader> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("AuthorizationHeader", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: AuthorizationHeader) {
            encoder.encodeString(value.toString())
        }

        override fun deserialize(decoder: Decoder): AuthorizationHeader {
            return parse(decoder.decodeString())
        }
    }
}

enum class AuthenticationSchema {
    Basic,
    Bearer
    ;

    companion object {

        private val lowerCaseValues: Map<String, AuthenticationSchema> = values().associateBy { it.name.lowercase() }

        fun parse(value: String): AuthenticationSchema? = lowerCaseValues[value.lowercase()]
    }
}
