package de.robolab.server.externaljs.jsonwebtoken

import de.robolab.common.net.headers.AuthorizationHeader
import de.robolab.server.config.Config
import de.robolab.common.externaljs.JSArray
import de.robolab.common.externaljs.dynamicOfDefined
import de.robolab.common.externaljs.toJSArray
import kotlinx.datetime.Instant

private val jwt: dynamic = js("require(\"jsonwebtoken\")")

fun jwtSign(payload: dynamic, secret: String): String = jwt.sign(payload, secret).unsafeCast<String>()
fun jwtSignRaw(payload: dynamic, secret: String, options: dynamic): String =
    jwt.sign(payload, secret, options).unsafeCast<String>()

fun jwtSign(
    payload: dynamic, secret: String,
    algorithm: String? = undefined,
    expiresIn: String? = undefined,
    notBefore: String? = undefined,
    audience: String? = undefined,
    issuer: String? = undefined,
    jwtid: String? = undefined,
    subject: String? = undefined,
    noTimestamp: Any? = undefined,
    header: String? = undefined,
    keyid: String? = undefined
): String = jwtSignRaw(
    payload, secret, dynamicOfDefined(
        "algorithm" to algorithm,
        "expiresIn" to expiresIn,
        "notBefore" to notBefore,
        "audience" to audience,
        "issuer" to issuer,
        "jwtid" to jwtid,
        "subject" to if(subject != undefined){
            if(payload.subject == subject) undefined
            else subject
        }else{
            undefined
        },
        "noTimestamp" to noTimestamp,
        "header" to header,
        "keyid" to keyid
    )
)

fun jwtVerify(token: String, secret: String): JSONWebToken = JSONWebToken(jwt.verify(token, secret), token)
fun jwtVerifyRaw(token: String, secret: String, options: dynamic): dynamic =
    jwt.verify(token, secret, options)

fun jwtVerify(
    token: String, secret: String,
    algorithms: JSArray<String>? = undefined,
    audience: String? = undefined,
    issuer: String? = undefined,
    jwtid: String? = undefined,
    subject: String? = undefined,
    clockTolerance: String? = undefined,
    maxAge: String? = undefined,
    nonce: String? = undefined
): JSONWebToken = JSONWebToken(
    jwtVerifyRaw(
        token,
        secret,
        dynamicOfDefined(
            "algorithms" to algorithms,
            "audience" to audience,
            "issuer" to issuer,
            "jwtid" to jwtid,
            "subject" to subject,
            "clockTolerance" to clockTolerance,
            "maxAge" to maxAge,
            "nonce" to nonce
        )
    ), token
)


class JSONWebToken internal constructor(val dynamic: dynamic, val rawToken: String) {
    val issuer: String? = dynamic.iss as? String
    val subject: String? = dynamic.sub as? String
    val audience: String? = dynamic.aud as? String
    val expirationTimeRaw: Long? = dynamic.exp as? Long
    val expirationTime: Instant? = if (expirationTimeRaw != null) Instant.fromEpochMilliseconds(expirationTimeRaw) else null
    val notBeforeTimeRaw: Long? = dynamic.nbf as? Long
    val notBeforeTime: Instant? = if (notBeforeTimeRaw != null) Instant.fromEpochMilliseconds(notBeforeTimeRaw) else null
    val issuedAtTimeRaw: Long? = dynamic.iat as? Long
    val issuedAtTime: Instant? = if (issuedAtTimeRaw != null) Instant.fromEpochMilliseconds(issuedAtTimeRaw) else null
    val jwtID: String? = dynamic.jti as? String

    fun asHeader(): AuthorizationHeader.Bearer = AuthorizationHeader.Bearer(rawToken)

    companion object {
        fun createSigned(
            payload: dynamic,
            privateKey: String,
            algorithm: String,
            subject: String? = undefined,
            issuer: String = Config.Auth.tokenIssuer,
            expiresIn: String? = Config.Auth.tokenExpiration,
            notBefore: String? = Config.Auth.tokenNotBefore
        ): JSONWebToken {

            return JSONWebToken(
                payload, jwtSign(
                    payload,
                    privateKey,
                    algorithm,
                    expiresIn,
                    notBefore,
                    issuer = issuer,
                    subject = subject
                )
            )
        }
    }
}

fun AuthorizationHeader.Bearer.parseJWT(secret: String, algorithms: List<String>, issuer: String): JSONWebToken {
    return jwtVerify(token, secret, algorithms.toJSArray(), issuer = issuer)
}