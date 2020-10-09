package de.robolab.server.auth

import de.robolab.common.net.headers.AuthorizationHeader
import de.robolab.server.config.Config
import de.robolab.server.externaljs.jsonwebtoken.JSONWebToken
import de.robolab.server.externaljs.jsonwebtoken.jwtSign
import de.robolab.server.externaljs.jsonwebtoken.parseJWT
import de.robolab.server.jsutils.jsTruthy
import de.robolab.server.jsutils.promise
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.random.Random
import kotlin.random.nextUInt


typealias ShareCode = UInt
class AuthService(val strictProvideOrder: Boolean = true, val random: Random=Random) {

    private val pendingShareRequests: MutableMap<ShareCode,Pair<UserID, (JSONWebToken)->Unit>> = mutableMapOf()
    private val activeShareCodes: MutableSet<ShareCode> = mutableSetOf()
    private val providedShares: MutableMap<ShareCode,Pair<UserID,JSONWebToken>> = mutableMapOf()

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

    fun obtainUser(header: AuthorizationHeader.Bearer): User?{
        val token: JSONWebToken
        try{
            token = header.parseJWT(Config.Auth.tokenPublicKey,listOf(Config.Auth.tokenAlgorithm),"robolab-renderer")
        }catch (ex: dynamic){
            if(ex.name != "JsonWebTokenError")
                throw ex as Throwable
            return null
        }
        return obtainUser(token)
    }

    fun createShareCode(): ShareCode {
        var code: ShareCode
        do{
            code = random.nextUInt()
        } while(!activeShareCodes.add(code))
        return code
    }

    fun provideSharedToken(code: ShareCode, token: JSONWebToken, userID: UserID) {
        val request = pendingShareRequests[code]
        if(request != null){
            if(request.first == userID){
                pendingShareRequests.remove(code)
                activeShareCodes.remove(code)
                request.second(token)
                return
            }else
                throw IllegalArgumentException("Providing UserID does not match Requesting UserID")
        }else if(strictProvideOrder)
            throw IllegalStateException("StrictProvideOrder is enabled and the Token was received before it was requested")
        if(providedShares.containsKey(code))
            throw IllegalArgumentException("Code is already providing a Token")
        activeShareCodes.add(code)
        providedShares[code] = userID to token
    }

    suspend fun getSharedToken(code: ShareCode, userID: UserID): JSONWebToken {
        val providedValue = providedShares[code]
        if(providedValue != null){
            if(providedValue.first == userID){
                providedShares.remove(code)
                activeShareCodes.remove(code)
                return providedValue.second
            }
            else
                throw IllegalArgumentException("Requesting UserID does not match Providing UserID")
        }
        activeShareCodes.add(code)
        if(pendingShareRequests.containsKey(code))
            throw IllegalArgumentException("Code is already requesting a Token-Share")
        var token: JSONWebToken? = null
        pendingShareRequests[code]=userID to {
            token = it
        }
        return Promise<JSONWebToken>{ resolve, _ ->
            if(token == null)
                pendingShareRequests[code] = userID to resolve
            else
                resolve(token!!)
        }.await()
    }

}