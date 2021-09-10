package de.robolab.client.net.requests.auth

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

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

        override fun deserialize(decoder: Decoder): TokenResponse {
            if (decoder.decodeNotNullMark()) {
                val error: String = decoder.decodeString()
                return when (error) {
                    ContinueTokenResponse.AuthorizationPending.error -> ContinueTokenResponse.AuthorizationPending
                    ContinueTokenResponse.SlowDown.error -> ContinueTokenResponse.SlowDown
                    ExpiredToken.error -> ExpiredToken
                    FinalTokenResponse.AccessDenied.error -> FinalTokenResponse.AccessDenied
                    else -> throw SerializationException("Failed to deserialize TokenResponse with error-field \"$error\"")
                }
            } else {
                decoder.decodeNull()
                val tokenType: String =
                    if (decoder.decodeNotNullMark()) decoder.decodeString() else throw SerializationException("Expected \"token_type\" on a TokenResponse without error-field")
                val accessToken: String =
                    if (decoder.decodeNotNullMark()) decoder.decodeString() else throw SerializationException("Expected \"access_token\" on a TokenResponse without error-field")
                val expires: Long? =
                    if (decoder.decodeNotNullMark()) decoder.decodeLong() else decoder.decodeNull()
                val refreshToken: String? =
                    if (decoder.decodeNotNullMark()) decoder.decodeString() else decoder.decodeNull()
                val scope: String? =
                    if(decoder.decodeNotNullMark()) decoder.decodeString() else decoder.decodeNull()
                return FinalTokenResponse.AccessToken(accessToken, refreshToken, tokenType, expires, scope)
            }
        }

        override fun serialize(encoder: Encoder, value: TokenResponse) {
            when {
                value is FinalTokenResponse.AccessToken -> {
                    encoder.encodeNull()
                    encoder.encodeNotNullMark()
                    encoder.encodeString(value.tokenType)
                    encoder.encodeNotNullMark()
                    encoder.encodeString(value.accessToken)
                    if (value.expiresIn != null) {
                        encoder.encodeNotNullMark()
                        encoder.encodeLong(value.expiresIn)
                    } else encoder.encodeNull()
                    if (value.refreshToken != null) {
                        encoder.encodeNotNullMark()
                        encoder.encodeString(value.refreshToken)
                    } else encoder.encodeNull()
                    if (value.scope != null) {
                        encoder.encodeNotNullMark()
                        encoder.encodeString(value.scope)
                    } else encoder.encodeNull()
                }
                value.error != null -> {
                    encoder.encodeNotNullMark()
                    encoder.encodeString(value.error)
                    encoder.encodeNull()
                    encoder.encodeNull()
                    encoder.encodeNull()
                    encoder.encodeNull()
                    encoder.encodeNull()
                }
                else -> throw SerializationException("Cannot not serialize non-AccessToken TokenResponse without \"error\" field")
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
            val refreshToken: String?,
            @SerialName("token_type")
            val tokenType: String,
            @SerialName("expires_in")
            val expiresIn: Long?,
            val scope: String?,
        ) : FinalTokenResponse(null)
    }
}