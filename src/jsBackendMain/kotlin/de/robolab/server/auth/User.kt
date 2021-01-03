package de.robolab.server.auth

import de.robolab.common.auth.AccessLevel
import de.robolab.common.auth.User
import de.robolab.common.externaljs.dynamicOf

fun User.toJWTPayload(): dynamic = dynamicOf(
    "sub" to userID.toString(),
    "accessLevel" to accessLevel.numericLevel,
    "username" to username,
    "group" to group
)

fun userFromJWTPayload(payload: dynamic): User? {
    val sub = payload.sub as? String ?: return null
    val accessLevel = AccessLevel.forNumeric(payload.accessLevel as? Int ?: return null) ?: return null
    val username = payload.username as? String ?: sub
    val group = payload.group as? Int?
    return User(sub.toUInt(), accessLevel, group, username)
}
