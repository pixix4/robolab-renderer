package de.robolab.common.utils

import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object BuildInformation {

    private val dataProperty = property<Map<String, String>>(emptyMap())

    val buildTimeProperty = dataProperty.mapBinding { data ->
        data["build.time"]  ?: ""
    }
    val buildTime by buildTimeProperty

    val buildToolsProperty = dataProperty.mapBinding { data ->
        data["build.tools"]  ?: ""
    }
    val buildTools by buildToolsProperty

    val buildSystemProperty = dataProperty.mapBinding { data ->
        data["build.system"]  ?: ""
    }
    val buildSystem by buildSystemProperty

    val buildUserProperty = dataProperty.mapBinding { data ->
        data["build.user"]  ?: ""
    }
    val buildUser by buildUserProperty


    val vcsBranchProperty = dataProperty.mapBinding { data ->
        data["vcs.branch"]  ?: ""
    }
    val vcsBranch by vcsBranchProperty

    val vcsCommitProperty = dataProperty.mapBinding { data ->
        data["vcs.commit"]  ?: ""
    }
    val vcsCommit by vcsCommitProperty

    val vcsTagProperty = dataProperty.mapBinding { data ->
        data["vcs.tag"]  ?: ""
    }
    val vcsTag by vcsTagProperty

    val vcsLastTagProperty = dataProperty.mapBinding { data ->
        data["vcs.lastTag"]  ?: ""
    }
    val vcsLastTag by vcsLastTagProperty

    val vcsDirtyProperty = dataProperty.mapBinding { data ->
        data["vcs.dirty"]?.toBoolean()  ?: false
    }
    val vcsDirty by vcsDirtyProperty


    val versionClientProperty = dataProperty.mapBinding { data ->
        Version.parse(data["version.client"] ?: "")
    }
    val versionClient by versionClientProperty
    val versionServerProperty = dataProperty.mapBinding { data ->
        Version.parse(data["version.server"] ?: "")
    }
    val versionServer by versionServerProperty


    init {
        val buildInformation = getBuildInformation()

        if (buildInformation == null) {
            GlobalScope.launch(Dispatchers.Main) {
                dataProperty.value = IniConverter.fromString(getAsyncBuildInformation())
            }
        } else {
            dataProperty.value = IniConverter.fromString(buildInformation)
        }

    }
}

expect fun getBuildInformation(): String?
expect suspend fun getAsyncBuildInformation(): String
