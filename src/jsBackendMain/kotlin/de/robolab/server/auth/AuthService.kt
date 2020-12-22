package de.robolab.server.auth

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeSpan
import de.robolab.common.auth.User
import de.robolab.common.auth.UserID
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.headers.AuthorizationHeader
import de.robolab.server.net.RESTResponseCodeException
import de.robolab.server.config.Config
import de.robolab.server.externaljs.jsonwebtoken.JSONWebToken
import de.robolab.server.externaljs.jsonwebtoken.parseJWT
import kotlin.random.Random
import kotlin.random.nextUInt


typealias ShareCode = UInt

class AuthService(val random: Random = Random, val shareCodeTimeout: TimeSpan = TimeSpan(15.0 * 60 * 1000)) {

    private val activeShareCodes: MutableSet<ShareCode> = mutableSetOf()
    private val nonSharingCodes: MutableSet<ShareCode> = mutableSetOf()
    private val providedShares: MutableMap<ShareCode, Pair<UserID?, JSONWebToken?>> = mutableMapOf()
    private val codeTimeouts: MutableList<Pair<DateTime, ShareCode>> = mutableListOf()

    fun obtainToken(user: User): JSONWebToken {
        return JSONWebToken.createSigned(
            user.toJWTPayload(),
            Config.Auth.tokenPrivateKey,
            Config.Auth.tokenAlgorithm,
        )
    }

    fun obtainUser(token: JSONWebToken): User? {
        return userFromJWTPayload(token.dynamic)
    }

    fun obtainUser(header: AuthorizationHeader.Bearer): User? {
        val token: JSONWebToken
        try {
            token = header.parseJWT(
                Config.Auth.tokenPublicKey,
                listOf(Config.Auth.tokenAlgorithm),
                issuer = Config.Auth.tokenIssuer
            )
        } catch (ex: dynamic) {
            if (ex.name != "JsonWebTokenError" && ex.name != "TokenExpiredError")
                throw ex as Throwable
            return null
        }
        return obtainUser(token)
    }

    fun createShareCode(sharing: Boolean): ShareCode {
        var code: ShareCode
        runCodeTimeouts()
        do {
            code = random.nextUInt()
        } while (!activeShareCodes.add(code))
        if (!sharing)
            nonSharingCodes.add(code)
        codeTimeouts.add(DateTime.now() + shareCodeTimeout to code)
        return code
    }

    fun runCodeTimeouts() = runCodeTimeouts(DateTime.now())

    fun runCodeTimeouts(time: DateTime) {
        while (codeTimeouts.isNotEmpty() && codeTimeouts.first().first < time) {
            val shareCode = codeTimeouts.removeFirst().second
            timeoutCode(shareCode)
        }
    }

    private fun timeoutCode(code: ShareCode) {
        providedShares.remove(code)
        nonSharingCodes.remove(code)
        activeShareCodes.remove(code)
    }

    fun assertCodeExists(code: ShareCode): Unit {
        if (code !in activeShareCodes)
            throw RESTResponseCodeException(HttpStatusCode.NotFound, "Code does not exist")
    }

    fun assertCanProvide(code: ShareCode): Unit {
        assertCodeExists(code)
        if (code in nonSharingCodes) return
        if (code !in providedShares)
            return
        else
            throw RESTResponseCodeException(HttpStatusCode.BadRequest, "Code is already providing a Token")
    }

    // Returns true if the ShareCode is used in a share-process, false otherwise
    fun provideSharedToken(code: ShareCode, token: JSONWebToken, userID: UserID? = null): Boolean =
        _provideSharedToken(code, token, userID)

    private fun _provideSharedToken(code: ShareCode, token: JSONWebToken?, userID: UserID?): Boolean {
        assertCodeExists(code)
        if (code in nonSharingCodes) {
            nonSharingCodes.remove(code)
            val index = codeTimeouts.indexOfFirst { it.second == code }
            if (index >= 0)
                codeTimeouts.removeAt(index)
            activeShareCodes.remove(code)
            return false
        }
        if (providedShares.containsKey(code))
            throw RESTResponseCodeException(HttpStatusCode.BadRequest, "Code is already providing a Token")
        providedShares[code] = userID to token
        return true
    }

    fun getSharedToken(code: ShareCode, userID: UserID? = null): JSONWebToken? {
        assertCodeExists(code)
        val providedValue = providedShares[code] ?: return null
        if (userID == null || providedValue.first == null || providedValue.first == userID) {
            providedShares.remove(code)
            val index = codeTimeouts.indexOfFirst { it.second == code }
            if (index >= 0)
                codeTimeouts.removeAt(index)
            activeShareCodes.remove(code)
            return providedValue.second
        } else
            throw RESTResponseCodeException(
                HttpStatusCode.Forbidden,
                "Requesting UserID does not match Providing UserID"
            )
    }

    fun abortShare(code: ShareCode) = _provideSharedToken(code, null, null)
}