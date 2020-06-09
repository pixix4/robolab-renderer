package de.robolab.common.utils

import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object BuildInformation {

    private val dataProperty = property<Map<String, String>>(emptyMap())

    val buildTimeProperty = dataProperty.mapBinding { data ->
        data["build.time"] ?: ""
    }
    val buildTime by buildTimeProperty

    val buildJavaVersionProperty = dataProperty.mapBinding { data ->
        data["build.javaVersion"] ?: ""
    }
    val buildJavaVersion by buildJavaVersionProperty

    val buildJavaVendorProperty = dataProperty.mapBinding { data ->
        data["build.javaVendor"] ?: ""
    }
    val buildJavaVendor by buildJavaVendorProperty

    val buildGradleVersionProperty = dataProperty.mapBinding { data ->
        data["build.gradleVersion"] ?: ""
    }
    val buildGradleVersion by buildGradleVersionProperty

    val buildSystemNameProperty = dataProperty.mapBinding { data ->
        data["build.systemName"] ?: ""
    }
    val buildSystemName by buildSystemNameProperty

    val buildSystemVersionProperty = dataProperty.mapBinding { data ->
        data["build.systemVersion"] ?: ""
    }
    val buildSystemVersion by buildSystemVersionProperty

    val buildUserProperty = dataProperty.mapBinding { data ->
        data["build.user"] ?: ""
    }
    val buildUser by buildUserProperty


    val vcsBranchProperty = dataProperty.mapBinding { data ->
        data["vcs.branch"] ?: ""
    }
    val vcsBranch by vcsBranchProperty

    val vcsCommitHashProperty = dataProperty.mapBinding { data ->
        data["vcs.commitHash"] ?: ""
    }
    val vcsCommitHash by vcsCommitHashProperty

    val vcsCommitMessageProperty = dataProperty.mapBinding { data ->
        data["vcs.commitMessage"] ?: ""
    }
    val vcsCommitMessage by vcsCommitMessageProperty

    val vcsCommitTimeProperty = dataProperty.mapBinding { data ->
        data["vcs.commitTime"] ?: ""
    }
    val vcsCommitTime by vcsCommitTimeProperty

    val vcsTagsProperty = dataProperty.mapBinding { data ->
        (data["vcs.tags"] ?: "").split(",").map { it.trim() }
    }
    val vcsTags by vcsTagsProperty

    val vcsLastTagProperty = dataProperty.mapBinding { data ->
        data["vcs.lastTag"] ?: ""
    }
    val vcsLastTag by vcsLastTagProperty

    val vcsLastTagDiffProperty = dataProperty.mapBinding { data ->
        data["vcs.lastTagDiff"]?.toIntOrNull() ?: 0
    }
    val vcsLastTagDiff by vcsLastTagDiffProperty

    val vcsDirtyProperty = dataProperty.mapBinding { data ->
        data["vcs.dirty"]?.toBoolean() ?: false
    }
    val vcsDirty by vcsDirtyProperty

    val vcsCommitCountProperty = dataProperty.mapBinding { data ->
        data["vcs.commitCount"]?.toBoolean() ?: false
    }
    val vcsCommitCount by vcsCommitCountProperty


    val versionClientProperty = dataProperty.mapBinding { data ->
        Version.parse(data["version.client"] ?: "")
    }
    val versionClient by versionClientProperty

    val versionServerProperty = dataProperty.mapBinding { data ->
        Version.parse(data["version.server"] ?: "")
    }
    val versionServer by versionServerProperty

    val dataMap: List<Pair<String, List<Pair<String, ObservableValue<Any>>>>>
    init {
        val buildInformation = getBuildInformation()

        if (buildInformation == null) {
            GlobalScope.launch(Dispatchers.Main) {
                dataProperty.value = IniConverter.fromString(getAsyncBuildInformation())
            }
        } else {
            dataProperty.value = IniConverter.fromString(buildInformation)
        }
        val runtime = getRuntimeInformation()

        val versionList = "Version" to listOf(
            "Client" to versionClientProperty,
            "Server" to versionServerProperty
        )
        val buildList = "Build" to listOf(
            "Time" to buildTimeProperty,
            "JavaVersion" to buildJavaVersionProperty,
            "JavaVendor" to buildJavaVendorProperty,
            "GradleVersion" to buildGradleVersionProperty,
            "SystemName" to buildSystemNameProperty,
            "SystemVersion" to buildSystemVersionProperty,
            "User" to buildUserProperty
        )
        val gitList = "Git" to listOf(
            "Branch" to vcsBranchProperty,
            "Commit" to vcsCommitHashProperty,
            "CommitMessage" to vcsCommitMessageProperty,
            "CommitTime" to vcsCommitTimeProperty,
            "Tags" to vcsTagsProperty,
            "LastTag" to vcsLastTagProperty,
            "LastTagDiff" to vcsLastTagDiffProperty,
            "Dirty" to vcsDirtyProperty,
            "CommitCount" to vcsCommitCountProperty
        )

        dataMap = if (runtime.isEmpty()) {
            listOf(
                versionList,
                buildList,
                gitList
            )
        } else {
            listOf(
                versionList,
                "Runtime" to runtime.toList(),
                buildList,
                gitList
            )
        }
    }
}

expect fun getBuildInformation(): String?
expect suspend fun getAsyncBuildInformation(): String

expect fun getRuntimeInformation(): List<Pair<String, ObservableValue<Any>>>