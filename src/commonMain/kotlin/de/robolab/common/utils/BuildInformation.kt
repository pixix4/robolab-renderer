package de.robolab.common.utils

import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface IBuildInformation {
    val versionFrontend: Version
    val versionBackend: Version

    val buildTime: String
    val buildJavaVersion: String
    val buildJavaVendor: String
    val buildGradleVersion: String
    val buildSystemName: String
    val buildSystemVersion: String
    val buildUser: String

    val vcsBranch: String
    val vcsCommitHash: String
    val vcsCommitMessage: String
    val vcsCommitTime: String
    val vcsTags: List<String>
    val vcsLastTag: String
    val vcsLastTagDiff: Int
    val vcsDirty: Boolean
    val vcsCommitCount: Int
}

object BuildInformation: IBuildInformation {

    val data = getBuildInformation()
    val dataMap: List<Pair<String, List<Pair<String, ObservableValue<Any>>>>>

    override val versionFrontend by data.mapBinding { it.versionFrontend }
    override val versionBackend by data.mapBinding { it.versionBackend }
    override val buildTime by data.mapBinding { it.buildTime }
    override val buildJavaVersion by data.mapBinding { it.buildJavaVersion }
    override val buildJavaVendor by data.mapBinding { it.buildJavaVendor }
    override val buildGradleVersion by data.mapBinding { it.buildGradleVersion }
    override val buildSystemName by data.mapBinding { it.buildSystemName }
    override val buildSystemVersion by data.mapBinding { it.buildSystemVersion }
    override val buildUser by data.mapBinding { it.buildUser }
    override val vcsBranch by data.mapBinding { it.vcsBranch }
    override val vcsCommitHash by data.mapBinding { it.vcsCommitHash }
    override val vcsCommitMessage by data.mapBinding { it.vcsCommitMessage }
    override val vcsCommitTime by data.mapBinding { it.vcsCommitTime }
    override val vcsTags by data.mapBinding { it.vcsTags }
    override val vcsLastTag by data.mapBinding { it.vcsLastTag }
    override val vcsLastTagDiff by data.mapBinding { it.vcsLastTagDiff }
    override val vcsDirty by data.mapBinding { it.vcsDirty }
    override val vcsCommitCount by data.mapBinding { it.vcsCommitCount }

    init {
        val runtime = try {
            getRuntimeInformation()
        } catch (e: Exception) {
            Logger("BuildInformation").warn { "Cannot get runtime information!" }
            Logger("BuildInformation").warn { e }
            emptyList()
        }

        val versionList = "Version" to listOf(
            "Frontend" to data.mapBinding { it.versionFrontend },
            "Backend" to data.mapBinding { it.versionBackend }
        )
        val buildList = "Build" to listOf(
            "Time" to data.mapBinding { it.buildTime },
            "Java version" to data.mapBinding { it.buildJavaVersion },
            "Java vendor" to data.mapBinding { it.buildJavaVendor },
            "Gradle version" to data.mapBinding { it.buildGradleVersion },
            "System name" to data.mapBinding { it.buildSystemName },
            "System version" to data.mapBinding { it.buildSystemVersion },
            "User" to data.mapBinding { it.buildUser }
        )
        val gitList = "Git" to listOf(
            "Branch" to data.mapBinding { it.vcsBranch },
            "Commit message" to data.mapBinding { it.vcsCommitMessage },
            "Commit hash" to data.mapBinding { it.vcsCommitHash },
            "Commit time" to data.mapBinding { it.vcsCommitTime },
            "Commit tags" to data.mapBinding { it.vcsTags.joinToString("") },
            "Latest tag" to data.mapBinding {
                it.vcsLastTag + if (it.vcsLastTagDiff > 0) " +${it.vcsLastTagDiff}" else ""
            },
            "Dirty build" to data.mapBinding { it.vcsDirty },
            "Commit count" to data.mapBinding { it.vcsCommitCount }
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

expect fun getBuildInformation(): ObservableValue<IBuildInformation>
expect fun getRuntimeInformation(): List<Pair<String, ObservableValue<Any>>>
