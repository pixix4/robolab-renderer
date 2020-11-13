package de.robolab.common.auth

import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.IRESTStatusProvider
import de.robolab.common.net.MIMEType
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

typealias UserID = UInt

object UserIDSerializer : KSerializer<UserID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UserIDSerializer", kind = PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): UserID = decoder.decodeInt().toUInt()

    override fun serialize(encoder: Encoder, value: UserID) = encoder.encodeInt(value.toInt())
}

//@Serializable(with = AccessLevelSerializer::class)
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

    infix fun satisfies(permissionLevel: AccessLevel): Boolean {
        return permissionLevel.numericLevel <= numericLevel
    }

    infix fun satisfiedBy(userLevel: AccessLevel): Boolean {
        return numericLevel <= userLevel.numericLevel
    }

    override fun toString(): String = "$name($numericLevel)"
}

object AccessLevelSerializer : KSerializer<AccessLevel> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("AccessLevelSerializer", kind = PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): AccessLevel = AccessLevel.forNumeric(decoder.decodeInt())!!

    override fun serialize(encoder: Encoder, value: AccessLevel) = encoder.encodeInt(value.numericLevel)
}


@Serializable
class User private constructor(
    @SerialName("userID")
    private val _userID: Int,
    val accessLevel: AccessLevel,
    val name: String,
    val group: Int? = null
) {


    constructor(
        userID: UserID,
        accessLevel: AccessLevel,
        group: Int? = null
    ) : this(
        userID, accessLevel, group, name = when {
            accessLevel == AccessLevel.Anonymous -> "anonymous"
            accessLevel >= AccessLevel.Tutor -> accessLevel.name
            group != null -> "group-$group"
            else -> userID.toString()
        }
    )

    constructor(
        userID: UserID,
        accessLevel: AccessLevel,
        group: Int? = null,
        name: String
    ) : this(
        userID.toInt(),
        accessLevel,
        name,
        group,
    )

    @Transient
    val userID: UserID = _userID.toUInt()


    fun canAccess(accessLevel: AccessLevel) = accessLevel <= this.accessLevel


    val internalName: String
        get() = if (this.userID == Anonymous.userID) "anonymous" else userID.toString()


    companion object {
        val Anonymous: User = User(UserID.MAX_VALUE, AccessLevel.Anonymous, null, name = "anonymous")
    }


    override fun toString(): String = "\"$name\"('$internalName')"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as User

        if (userID != other.userID) return false
        if (accessLevel != other.accessLevel) return false
        if (group != other.group) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userID.hashCode()
        result = 31 * result + accessLevel.hashCode()
        result = 31 * result + (group ?: 0)
        return result
    }
}

abstract class UserPermissionException : RuntimeException, IRESTStatusProvider {
    val user: User
    final override val code: HttpStatusCode
    final override val mimeType: MIMEType?

    constructor(
        user: User,
        message: String = "The user $user does not have the required permissions",
        statusCode: HttpStatusCode = HttpStatusCode.Forbidden,
        mimeType: MIMEType? = MIMEType.PlainText
    ) : super(message) {
        this.user = user
        this.code = statusCode
        this.mimeType = mimeType
    }

    constructor(
        user: User,
        message: String = "The user '$user' does not have the required permissions",
        cause: Throwable?,
        statusCode: HttpStatusCode = HttpStatusCode.Forbidden,
        mimeType: MIMEType? = MIMEType.PlainText
    ) : super(message, cause) {
        this.user = user
        this.code = statusCode
        this.mimeType = mimeType
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

class UserAccessLevelPermissionException : UserPermissionException {
    val requiredLevel: AccessLevel


    constructor(
        user: User,
        requiredLevel: AccessLevel,
        message: String = "The user $user does not have the required permission-Level of " +
                "$requiredLevel, only ${user.accessLevel}"
    ) : super(user, message) {
        this.requiredLevel = requiredLevel
    }

    constructor(
        user: User,
        requiredLevel: AccessLevel,
        message: String = "The user $user does not have the required permission-Level of " +
                "$requiredLevel, only ${user.accessLevel}",
        cause: Throwable?
    ) : super(user, message, cause) {
        this.requiredLevel = requiredLevel
    }
}

fun User.requireAccess(accessLevel: AccessLevel) {
    if (!canAccess(accessLevel)) {
        if (this == User.Anonymous)
            throw UserUnauthorizedException()
        else
            throw UserAccessLevelPermissionException(this, accessLevel)
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