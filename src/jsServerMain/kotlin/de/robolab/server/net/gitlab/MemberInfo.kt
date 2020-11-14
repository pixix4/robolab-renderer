package de.robolab.server.net.gitlab

import kotlinx.serialization.Serializable

@Serializable
data class MemberInfo(
    val id: Int,
    val name: String,
    val username: String,
    val state: String,
    val access_level: Int,
    val created_at: String,
)

val MemberInfo.isActive
    get() = this.state == "active"
