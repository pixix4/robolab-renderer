package de.robolab.server.auth

import de.robolab.server.externaljs.dynamicOf

typealias UserID = UInt
class User(val userID: UserID, val isRoboLab: Boolean) {
    fun toJWTPayload():dynamic = dynamicOf(
        "sub" to userID.toString(),
        "roboLvl" to if(isRoboLab) 10 else 0
    )

    companion object{
        val Anonymous: User = User(UserID.MAX_VALUE,false)
        fun fromJWTPayload(payload: dynamic): User?{
            val sub = payload.sub as? String ?: return null
            val roboLvl = payload.roboLvl as? Int ?: return null
            return User(sub.toUInt(),roboLvl>=10)
        }
    }

    override fun equals(other: Any?): Boolean {
        return (other is User) && (other.userID == userID)
    }

    override fun hashCode(): Int {
        return userID.hashCode()
    }
}