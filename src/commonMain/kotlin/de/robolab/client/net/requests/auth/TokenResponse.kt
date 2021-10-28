package de.robolab.client.net.requests.auth

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*

@Serializable(with = TokenResponse.TokenResponseSerializer::class)
sealed class TokenResponse(val error: String?) {

    object TokenResponseSerializer : KSerializer<TokenResponse> {
        override val descriptor: SerialDescriptor =
            buildClassSerialDescriptor("de.robolab.client.net.requests.auth.TokenResponse") {
                this.element<String?>("error", isOptional = true)
                this.element<String?>("token_type", isOptional = true)
                this.element<String?>("access_token", isOptional = true)
                this.element<Long?>("expires", isOptional = true)
                this.element<String?>("refresh_token", isOptional = true)
                this.element<String?>("scope", isOptional = true)
            }

        override fun deserialize(decoder: Decoder): TokenResponse = decoder.decodeStructure(descriptor) {
            var error: String? = null
            var tokenType: String? = null
            var accessToken: String? = null
            var expires: Long? = null
            var refreshToken: String? = null
            var scope: String? = null
            while (true) {
                when (val index = this.decodeElementIndex(descriptor)) {
                    0 -> error = decodeNullableSerializableElement(descriptor, 0, serializer<String?>())
                    1 -> tokenType = decodeNullableSerializableElement(descriptor, 0, serializer<String?>())
                    2 -> accessToken = decodeNullableSerializableElement(descriptor, 0, serializer<String?>())
                    3 -> expires = decodeNullableSerializableElement(descriptor, 0, serializer<Long?>())
                    4 -> refreshToken = decodeNullableSerializableElement(descriptor, 0, serializer<String?>())
                    5 -> scope = decodeNullableSerializableElement(descriptor, 0, serializer<String?>())
                    CompositeDecoder.DECODE_DONE -> break
                    else -> throw SerializationException("Unexpected index \"$index\"")
                }
            }
            if (error != null) {
                return when (error) {
                    ContinueTokenResponse.AuthorizationPending.error -> ContinueTokenResponse.AuthorizationPending
                    ContinueTokenResponse.SlowDown.error -> ContinueTokenResponse.SlowDown
                    ExpiredToken.error -> ExpiredToken
                    FinalTokenResponse.AccessDenied.error -> FinalTokenResponse.AccessDenied
                    else -> throw SerializationException("Failed to deserialize TokenResponse with error-field \"$error\"")
                }
            } else {
                return FinalTokenResponse.AccessToken(
                    accessToken ?: throw SerializationException("Expected field \"accessToken\" missing on non-error response"),
                    refreshToken,
                    tokenType ?: throw SerializationException("Expected field \"tokenType\" missing on non-error response"),
                    expires,
                    scope
                )
            }
        }

        override fun serialize(encoder: Encoder, value: TokenResponse) {
            encoder.encodeStructure(descriptor) {
                when {
                    value is FinalTokenResponse.AccessToken -> {
                        this.encodeStringElement(descriptor, 1, value.tokenType)
                        this.encodeStringElement(descriptor, 2, value.accessToken)
                        if(value.expiresIn != null) this.encodeNullableSerializableElement(descriptor, 3, serializer<Long?>(), value.expiresIn)
                        if(value.refreshToken != null) this.encodeNullableSerializableElement(descriptor, 4, serializer<String?>(), value.refreshToken)
                        if(value.scope != null) this.encodeNullableSerializableElement(descriptor, 5, serializer<String?>(), value.scope)
                    }
                    value.error != null -> {
                        this.encodeStringElement(descriptor, 0, value.error)
                    }
                    else -> throw SerializationException("Cannot not serialize non-AccessToken TokenResponse without \"error\" field")
                }
            }
        }
    }

    sealed class ContinueTokenResponse(error: String) : TokenResponse(error) {
        object SlowDown : ContinueTokenResponse("slow_down")
        object AuthorizationPending : ContinueTokenResponse("authorization_pending")
    }

    object ExpiredToken : TokenResponse("expired_token")
    sealed class FinalTokenResponse(error: String?) : TokenResponse(error) {
        object AccessDenied : FinalTokenResponse("access_denied")
        data class AccessToken(
            @SerialName("access_token")
            val accessToken: String,
            @SerialName("refresh_token")
            val refreshToken: String? = null,
            @SerialName("token_type")
            val tokenType: String,
            @SerialName("expires_in")
            val expiresIn: Long? = null,
            val scope: String? = null,
        ) : FinalTokenResponse(null)
    }
}
