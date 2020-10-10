package de.robolab.server.auth

import com.soywiz.klock.DateTime
import com.soywiz.klock.DateTimeSpan
import com.soywiz.klock.TimeSpan
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.headers.AuthorizationHeader
import de.robolab.server.RequestError
import de.robolab.server.config.Config
import de.robolab.server.externaljs.jsonwebtoken.JSONWebToken
import de.robolab.server.externaljs.jsonwebtoken.parseJWT
import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.random.Random
import kotlin.random.nextUInt


typealias ShareCode = UInt

class AuthService(val strictProvideOrder: Boolean = true, val random: Random = Random, val shareCodeTimeout:TimeSpan=TimeSpan(15.0*60*1000)) {

    private val pendingShareRequests: MutableMap<ShareCode, Pair<UserID?, (JSONWebToken?) -> Unit>> = mutableMapOf()
    private val activeShareCodes: MutableSet<ShareCode> = mutableSetOf()
    private val nonSharingCodes: MutableSet<ShareCode> = mutableSetOf()
    private val providedShares: MutableMap<ShareCode, Pair<UserID?, JSONWebToken?>> = mutableMapOf()
    private val codeTimeouts: MutableList<Pair<DateTime, ShareCode>> = mutableListOf()

    fun obtainToken(user: User): JSONWebToken {
        return JSONWebToken.createSigned(
            user.toJWTPayload(),
            Config.Auth.tokenPrivateKey,
            Config.Auth.tokenAlgorithm
        )
    }

    fun obtainUser(token: JSONWebToken): User? {
        return User.fromJWTPayload(token.dynamic)
    }

    fun obtainUser(header: AuthorizationHeader.Bearer): User? {
        val token: JSONWebToken
        try {
            token = header.parseJWT(Config.Auth.tokenPublicKey, listOf(Config.Auth.tokenAlgorithm), "robolab-renderer")
        } catch (ex: dynamic) {
            if (ex.name != "JsonWebTokenError")
                throw ex as Throwable
            return null
        }
        return obtainUser(token)
    }

    fun createShareCode(sharing: Boolean): ShareCode {
        var code: ShareCode
        do {
            code = random.nextUInt()
        } while (!activeShareCodes.add(code))
        if (!sharing)
            nonSharingCodes.add(code)
        codeTimeouts.add(DateTime.now() to code)
        return code
    }

    fun runCodeTimeouts() = runCodeTimeouts(DateTime.now())

    fun runCodeTimeouts(time:DateTime){
        while(codeTimeouts.isNotEmpty() && codeTimeouts.first().first < time ){
            val shareCode= codeTimeouts.removeFirst().second
            timeoutCode(shareCode)
        }
    }

    private fun timeoutCode(code: ShareCode){
        providedShares.remove(code)
        nonSharingCodes.remove(code)
        val pendingRequest = pendingShareRequests.remove(code)
        if(pendingRequest != null)
            try{
                pendingRequest.second(null)
            }catch(ex:Error){
                console.error("The following exception occurred while removing code \"$code\" due to timeout")
                ex.printStackTrace()
            }
        activeShareCodes.remove(code)
    }

    fun assertCodeExists(code: ShareCode): Unit {
        if (code !in activeShareCodes)
            throw RequestError(HttpStatusCode.NotFound, "Code does not exist", verbose = false)
    }

    fun assertCanProvide(code: ShareCode): Unit {
        assertCodeExists(code)
        if (code in nonSharingCodes) return
        if (code in pendingShareRequests) return
        if (!strictProvideOrder) {
            if (code !in providedShares)
                return
            else
                throw RequestError(HttpStatusCode.BadRequest, "Code is already providing a Token", verbose = false)
        }
        throw RequestError(
            HttpStatusCode.TooEarly,
            "StrictProvideOrder is enabled and the Token was received before it was requested",
            verbose = false
        )
    }

    // Returns true if the ShareCode is used in a share-process, false otherwise
    fun provideSharedToken(code:ShareCode, token:JSONWebToken, userID: UserID? = null): Boolean =
        _provideSharedToken(code,token,userID)
    private fun _provideSharedToken(code: ShareCode, token: JSONWebToken?, userID: UserID?): Boolean {
        assertCodeExists(code)
        if (code in nonSharingCodes) {
            nonSharingCodes.remove(code)
            val index = codeTimeouts.indexOfFirst { it.second == code }
            if(index >=0)
                codeTimeouts.removeAt(index)
            activeShareCodes.remove(code)
            return false
        }
        val request = pendingShareRequests[code]
        if (request != null) {
            if (userID == null || request.first == null || request.first == userID) {
                pendingShareRequests.remove(code)
                val index = codeTimeouts.indexOfFirst { it.second == code }
                if(index >=0)
                    codeTimeouts.removeAt(index)
                activeShareCodes.remove(code)
                request.second(token)
                return true
            } else
                throw RequestError(
                    HttpStatusCode.Forbidden,
                    "Providing UserID does not match Requesting UserID",
                    verbose = false
                )
        } else if (strictProvideOrder)
            throw RequestError(
                HttpStatusCode.TooEarly,
                "StrictProvideOrder is enabled and the Token was received before it was requested",
                verbose = false
            )
        if (providedShares.containsKey(code))
            throw RequestError(HttpStatusCode.BadRequest, "Code is already providing a Token", verbose = false)
        providedShares[code] = userID to token
        return true
    }

    suspend fun getSharedToken(code: ShareCode, userID: UserID? = null): JSONWebToken? {
        assertCodeExists(code)
        val providedValue = providedShares[code]
        if (providedValue != null) {
            if (userID == null || providedValue.first == null || providedValue.first == userID) {
                providedShares.remove(code)
                val index = codeTimeouts.indexOfFirst { it.second == code }
                if(index >=0)
                    codeTimeouts.removeAt(index)
                activeShareCodes.remove(code)
                return providedValue.second
            } else
                throw RequestError(
                    HttpStatusCode.Forbidden,
                    "Requesting UserID does not match Providing UserID",
                    verbose = false
                )
        }
        if (pendingShareRequests.containsKey(code))
            throw RequestError(HttpStatusCode.BadRequest, "Code is already requesting a Token-Share", verbose = false)
        var token: JSONWebToken? = null
        var tokenSet = false
        pendingShareRequests[code] = userID to {
            token = it
            tokenSet = true
        }
        return Promise<JSONWebToken?> { resolve, _ ->
            if (!tokenSet)
                pendingShareRequests[code] = userID to resolve
            else
                resolve(token)
        }.await()
    }

    fun abortShare(code: ShareCode) = _provideSharedToken(code,null,null)
}