package de.robolab.server.net.gitlab

import kotlinx.serialization.Serializable

@Serializable
data class NamespaceInfo(
    val id: Int,
    val name: String,
    val path: String,
    val kind: String,
    val full_path: String,
    val parent_id: Int,
)
