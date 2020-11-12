package de.robolab.server.auth

import de.robolab.common.net.HttpStatusCode
import de.robolab.server.externaljs.dynamicOf
import de.robolab.server.net.RESTResponseCodeException

typealias UserID = UInt

enum class AccessLevel(val numericLevel: Int) {
    Anonymous(Int.MIN_VALUE),
    LoggedIn(0),
    GroupMember(5),
    Tutor(10),
    Manager(30),
    Admin(40),
    ;

    companion object {
        fun forNumeric(accessLevel: Int): AccessLevel? {
            return values().singleOrNull { it.numericLevel == accessLevel }
        }
    }
}

class User(val userID: UserID, val accessLevel: Int) {

    fun canAccess(accessLevel: Int) = accessLevel <= this.accessLevel

    fun canAccess(accessLevel: AccessLevel) = canAccess(accessLevel.numericLevel)

    fun toJWTPayload(): dynamic = dynamicOf(
        "sub" to userID.toString(),
        "accessLevel" to accessLevel,
        "name" to if (accessLevel >= AccessLevel.Tutor.numericLevel) AccessLevel.forNumeric(accessLevel)
            ?: userID.toString() else userID.toString()
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
        message: String = "The user '${user.internalName}' does not have the required permission-Level of " +
                "$requiredLevel (${AccessLevel.forNumeric(requiredLevel)?.name ?: "?"}), only " +
                "${user.accessLevel} (${AccessLevel.forNumeric(user.accessLevel)?.name ?: "?"})"
    ) : super(user, message) {
        this.requiredLevel = requiredLevel
    }

    constructor(
        user: User,
        requiredLevel: Int,
        message: String = "The user '${user.internalName}' does not have the required permission-Level of " +
                "$requiredLevel (${AccessLevel.forNumeric(requiredLevel)?.name ?: "?"}), only " +
                "${user.accessLevel} (${AccessLevel.forNumeric(user.accessLevel)?.name ?: "?"})",
        cause: Throwable?
    ) : super(user, message, cause) {
        this.requiredLevel = requiredLevel
    }

    constructor(
        user: User,
        requiredLevel: AccessLevel,
        message: String = "The user '${user.internalName}' does not have the required permission-Level of " +
                "${requiredLevel.numericLevel} (${requiredLevel.name}), only " +
                "${user.accessLevel} (${AccessLevel.forNumeric(user.accessLevel)?.name ?: "?"})"
    ) : this(user, requiredLevel.numericLevel, message)

    constructor(
        user: User,
        requiredLevel: AccessLevel,
        message: String = "The user '${user.internalName}' does not have the required permission-Level of " +
                "${requiredLevel.numericLevel} (${requiredLevel.name}), only " +
                "${user.accessLevel} (${AccessLevel.forNumeric(user.accessLevel)?.name ?: "?"})",
        cause: Throwable?
    ) : this(user, requiredLevel.numericLevel, message, cause)


}

fun User.requireAccess(accessLevel: Int) {
    if (!canAccess(accessLevel)) {
        if (this == User.Anonymous)
            throw UserUnauthorizedException()
        else
            throw UserNumericPermissionException(this, accessLevel)
    }
}

fun User.requireAccess(accessLevel: AccessLevel) {
    if (!canAccess(accessLevel.numericLevel)) {
        if (this == User.Anonymous)
            throw UserUnauthorizedException()
        else
            throw UserNumericPermissionException(this, accessLevel)
    }
}

fun User.requireLogin() = requireAccess(AccessLevel.LoggedIn)
fun User.requireGroupMember() = requireAccess(AccessLevel.GroupMember)

fun User.requireTutor() = requireAccess(AccessLevel.Tutor)
fun User.requireManager() = requireAccess(AccessLevel.Manager)

fun User.requireAdmin() = requireAccess(AccessLevel.Admin)

val User.isLoggedIn: Boolean
    get() = canAccess(AccessLevel.LoggedIn)

val User.hasGroupMemberAccess: Boolean
    get() = canAccess(AccessLevel.GroupMember)

val User.hasTutorAccess: Boolean
    get() = canAccess(AccessLevel.Tutor)

val User.hasManagerAccess: Boolean
    get() = canAccess(AccessLevel.Manager)

val User.hasAdminAccess: Boolean
    get() = canAccess(AccessLevel.Admin)