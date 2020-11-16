@file:Suppress("MemberVisibilityCanBePrivate")

package de.robolab.server.net.gitlab

import de.robolab.client.net.IServerResponse
import de.robolab.client.net.http
import de.robolab.client.net.linksRelMap
import de.robolab.common.auth.UserID
import de.robolab.common.net.headers.AuthorizationHeader
import de.robolab.common.net.parseOrThrow
import de.robolab.common.utils.*
import de.robolab.server.config.Config
import kotlinx.serialization.builtins.ListSerializer

typealias MemberInfoMap = Map<UserID, MemberInfo>

object GitLabServer {
    private val apiHeader: AuthorizationHeader = AuthorizationHeader.Bearer(Config.Auth.gitlabAPIToken)
    val baseURL = Config.Auth.gitlabURL

    val defaultCacheDuration = Config.Auth.cacheDuration.toDuration()
    val missCacheDuration = Config.Auth.cacheDurationOnKeyMiss.toDuration()

    val userInfoCache: DefaultCachedMap<UserID, UserInfo> = cachedMap(defaultCacheDuration, ::requestUserInfo)

    val robolabTutorMapCache: CachedValue<MemberInfoMap> =
        cachedValue(defaultCacheDuration) {
            requestNamespaceMembers(Config.Auth.robolabGroupID)
        }

    val robolabGroupProjectsCache: CachedValue<GroupProjectsInfo<SimpleProjectInfo>> =
        cachedValue(defaultCacheDuration) {
            val projects =
                requestNamespaceProjectsSimple(Config.Auth.groupProjectsGroupID).filter { it.group_id != null }
            GroupProjectsInfo(projects.associate {
                it.group_id!! to GroupProjectsInfo.MemberedProjectInfo(it, requestProjectMembers(it.id))
            })
        }

    suspend fun requestProjectMembers(projectID: ProjectID): MemberInfoMap {
        val response = http {
            url("$baseURL/api/v4/projects/$projectID/members")
            header(apiHeader)
        }.exec()
        return response.joinPagination { parseOrThrow(ListSerializer(MemberInfo.serializer())) }
            .associateBy { it.id.toUInt() }
    }

    suspend fun requestNamespaceMembers(groupID: Int): MemberInfoMap {
        val response = http {
            url("$baseURL/api/v4/groups/$groupID/members")
            header(apiHeader)
        }.exec()
        return response.joinPagination { parseOrThrow(ListSerializer(MemberInfo.serializer())) }
            .associateBy { it.id.toUInt() }
    }

    suspend fun requestNamespaceProjects(groupID: Int): List<ProjectInfo> {
        val response = http {
            url("$baseURL/api/v4/groups/$groupID/projects")
            header(apiHeader)
        }.exec()
        return response.joinPagination { parseOrThrow(ListSerializer(ProjectInfo.serializer())) }
    }

    suspend fun requestNamespaceProjectsSimple(groupID: Int): List<SimpleProjectInfo> {
        val response = http {
            url("$baseURL/api/v4/groups/$groupID/projects")
            query("simple" to "true")
            header(apiHeader)
        }.exec()
        return response.joinPagination { parseOrThrow(ListSerializer(SimpleProjectInfo.serializer())) }
    }

    suspend fun requestUserInfo(userID: UserID): UserInfo {
        val response = http {
            url("$baseURL/api/v4/users/$userID")
            header(apiHeader)
        }.exec()
        return response.parseOrThrow(UserInfo.serializer())
    }

    suspend fun <T> IServerResponse.joinPagination(transform: IServerResponse.() -> List<T>): List<T> {
        var nextLink: String = this.linksRelMap["next"] ?: return transform()
        var currentResponse: IServerResponse = this
        val result: MutableList<T> = currentResponse.transform().toMutableList()
        do {
            currentResponse = http {
                url(nextLink)
                header(apiHeader)
            }.exec()
            result.addAll(currentResponse.transform())
            nextLink = currentResponse.linksRelMap["next"] ?: return result
        } while (true)
    }
}

data class GroupProjectsInfo<PI : IProjectInfo>(
    val projects: Map<ProjectID, MemberedProjectInfo<PI>>
) {
    data class MemberedProjectInfo<PI : IProjectInfo>(
        val project: PI,
        val members: MemberInfoMap
    )

    val members: Map<UserID, List<Pair<PI, MemberInfo>>> = projects.values.flatMap { (project, members) ->
        members.map { it.key to (project to it.value) }
    }.groupBy({ it.first }) { it.second }

    val groupProjects: Map<Int, MemberedProjectInfo<PI>> =
        projects.values.filter { it.project.group_id != null }.associateBy { it.project.group_id!! }
}

suspend fun GitLabServer.getRoboLabTutors(): MemberInfoMap = robolabTutorMapCache.getValue()

suspend fun GitLabServer.getGroupMembers(group: Int): MemberInfoMap? {
    var info = robolabGroupProjectsCache.getValue()
    val project = info.groupProjects[group]
    if (project != null) return project.members
    info = robolabGroupProjectsCache.getValue(missCacheDuration)
    return info.groupProjects[group]?.members
}

suspend fun GitLabServer.getGroupProjectsForUser(userID: UserID): List<Pair<SimpleProjectInfo, MemberInfo>> {
    var info = robolabGroupProjectsCache.getValue()
    val result = info.members[userID]
    if (result != null) return result
    info = robolabGroupProjectsCache.getValue(missCacheDuration)
    return info.members[userID].orEmpty()
}

suspend fun GitLabServer.getGroupIDsForUser(userID: UserID): List<Int> = getGroupProjectsForUser(userID).mapNotNull {
    it.first.group_id
}

suspend fun GitLabServer.getUserInfo(userID: UserID): UserInfo = userInfoCache.getUpdated(userID)
