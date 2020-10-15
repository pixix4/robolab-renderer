package de.robolab.server.auth

import de.robolab.common.net.RESTRequestError
import de.robolab.server.externaljs.dynamicOf

typealias UserID = UInt

class User(val userID: UserID, val roboLvl: Int) {
    fun toJWTPayload(): dynamic = dynamicOf(
        "sub" to userID.toString(),
        "roboLvl" to roboLvl
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
            val roboLvl = payload.roboLvl as? Int ?: return null
            return User(sub.toUInt(), roboLvl)
        }
    }

    override fun equals(other: Any?): Boolean {
        return (other is User) && (other.userID == userID)
    }

    override fun hashCode(): Int {
        return userID.hashCode()
    }
}

abstract class UserPermissionException : RESTRequestError {
    val user: User

    constructor(
        user: User,
        message: String = "The user '${user.internalName}' does not have the required permissions"
    ) : super(message) {
        this.user = user
    }

    constructor(
        user: User,
        message: String = "The user '${user.internalName}' does not have the required permissions",
        cause: Throwable?
    ) : super(message, cause) {
        this.user = user
    }
}

class UserNumericPermissionException : UserPermissionException {
    val requiredLevel: Int


    constructor(
        user: User,
        requiredLevel: Int,
        message: String = "The user '${user.internalName}' does not have the required permission-Level of $requiredLevel, only ${user.roboLvl}"
    ) : super(user, message) {
        this.requiredLevel = requiredLevel
    }

    constructor(
        user: User,
        requiredLevel: Int,
        message: String = "The user '${user.internalName}' does not have the required permission-Level of $requiredLevel, only ${user.roboLvl}",
        cause: Throwable?
    ) : super(user, message, cause) {
        this.requiredLevel = requiredLevel
    }
}

fun User.requireLogin(userLevel: Int) {
    if (userLevel < roboLvl) throw UserNumericPermissionException(this, userLevel)
}

fun User.requireLogin() = requireLogin(0)

fun User.requireTutor() = requireLogin(10)

fun User.requireAdmin() = requireLogin(40)