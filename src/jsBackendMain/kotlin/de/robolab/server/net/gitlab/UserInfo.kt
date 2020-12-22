package de.robolab.server.net.gitlab

import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    val id: Int,
    val username: String,
    val name: String,
    val state: String,
    val avatar_url: String?,
    val web_url: String,
    val created_at: String,
    val public_email: String
)
