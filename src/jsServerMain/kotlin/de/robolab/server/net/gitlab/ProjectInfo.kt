package de.robolab.server.net.gitlab

import de.robolab.server.config.Config
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

typealias ProjectID = Int

private val group_id_regex: Regex = Config.Auth.groupProjectsRegex.toRegex()

interface IProjectInfo {
    val id: ProjectID
    val name: String
    val name_with_namespace: String
    val path: String
    val path_with_namespace: String
    val created_at: String
    val web_url: String
    val namespace: NamespaceInfo

    @Transient
    val group_id: Int?
        get() = group_id_regex.find(path)?.destructured?.component1()?.toInt()
}

@Serializable
data class SimpleProjectInfo(
    override val id: ProjectID,
    override val name: String,
    override val name_with_namespace: String,
    override val path: String,
    override val path_with_namespace: String,
    override val created_at: String,
    override val web_url: String,
    override val namespace: NamespaceInfo,
) : IProjectInfo

@Serializable
data class ProjectInfo(
    override val id: ProjectID,
    override val name: String,
    override val name_with_namespace: String,
    override val path: String,
    override val path_with_namespace: String,
    override val created_at: String,
    override val web_url: String,
    override val namespace: NamespaceInfo,
    val empty_repo: Boolean,
    val creator_id: Int,
) : IProjectInfo
