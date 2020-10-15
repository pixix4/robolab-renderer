package de.robolab.server.auth

import de.robolab.common.net.HttpStatusCode
import de.robolab.server.externaljs.dynamicOf
import de.robolab.server.net.RESTResponseCodeException

typealias UserID = UInt

class User(val userID: UserID, val accessLevel: Int) {
    fun toJWTPayload(): dynamic = dynamicOf(
        "sub" to userID.toString(),
        "accessLevel" to accessLevel
    )

    fun toJSON(): dynamic {
        val payload: dynamic = toJWTPayload()
        payload.anonymous = this == Anonymous
        return payload
    }

    val internalName: String
        get() = if (this == Anonymous) "anonymous" else userID.toString()

    companion object {
        val Anonymous: User = User(UserID.MAX_VALUE, Int.MIN_VALUE)
        fun fromJWTPayload(payload: dynamic): User? {
            val sub = payload.sub as? String ?: return null
            val accessLevel = payload.accessLevel as? Int ?: return null
            return User(sub.toUInt(), accessLevel)
        }
    }

    override fun equals(other: Any?): Boolean {
        return (other is User) && (other.userID == userID)
    }

    override fun hashCode(): Int {
        return userID.hashCode()
    }
}

abstract class UserPermissionException : RESTResponseCodeException {
    val user: User

    constructor(
        user: User,
        message: String = "The user '${user.internalName}' does not have the required permissions",
        statusCode: HttpStatusCode = HttpStatusCode.Forbidden
    ) : super(statusCode, message) {
        this.user = user
    }

    constructor(
        user: User,
        message: String = "The user '${user.internalName}' does not have the required permissions",
        cause: Throwable?,
        statusCode: HttpStatusCode = HttpStatusCode.Forbidden
    ) : super(statusCode, message, cause = cause) {
        this.user = user
    }
}

class UserUnauthorizedException : UserPermissionException {
    constructor(message: String = "Authorization required") : super(
        User.Anonymous,
        message,
        HttpStatusCode.Unauthorized
    )

    constructor(message: String = "Authorization required", cause: Throwable?) : super(
        User.Anonymous,
        message,
        cause,
        HttpStatusCode.Unauthorized
    )
}

class UserNumericPermissionException : UserPermissionException {
    val requiredLevel: Int


    constructor(
        user: User,
        requiredLevel: Int,
        message: String = "The user '${user.internalName}' does not have the required permission-Level of $requiredLevel, only ${user.accessLevel}"
    ) : super(user, message) {
        this.requiredLevel = requiredLevel
    }

    constructor(
        user: User,
        requiredLevel: Int,
        message: String = "The user '${user.internalName}' does not have the required permission-Level of $requiredLevel, only ${user.accessLevel}",
        cause: Throwable?
    ) : super(user, message, cause) {
        this.requiredLevel = requiredLevel
    }
}

fun User.requireLogin(accessLevel: Int) {
    if (this == User.Anonymous) throw UserUnauthorizedException()
    if (this.accessLevel < accessLevel) throw UserNumericPermissionException(this, accessLevel)
}

fun User.requireLogin() = requireLogin(0)

fun User.requireTutor() = requireLogin(10)

fun User.requireAdmin() = requireLogin(40)