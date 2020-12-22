package de.robolab.common.utils

import de.robolab.server.externaljs.fs.existsSync
import de.robolab.server.externaljs.fs.readFileSync
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.observeConst

fun readFile(filename: String): String? {
    if (existsSync(filename)) {
        return readFileSync(filename, "utf8") as String
    }
    return null
}

actual fun getBuildInformation(): ObservableValue<IBuildInformation> {

    val fileContent =  readFile("build.ini") ?: readFile("build/processedResources/build.ini") ?: ""

    val data = IniConverter.fromString(fileContent)

    return object: IBuildInformation {
        override val versionFrontend = data["version.frontend"]?.let { Version.parse(it) } ?: Version.UNKNOWN
        override val versionBackend = data["version.backend"]?.let { Version.parse(it) } ?: Version.UNKNOWN

        override val buildTime = data["build.time"] ?: ""
        override val buildJavaVersion = data["build.javaVersion"] ?: ""
        override val buildJavaVendor = data["build.javaVendor"] ?: ""
        override val buildGradleVersion = data["build.gradleVersion"] ?: ""
        override val buildSystemName = data["build.systemName"] ?: ""
        override val buildSystemVersion = data["build.systemVersion"] ?: ""
        override val buildUser = data["build.user"] ?: ""

        override val vcsBranch = data["vcs.branch"] ?: ""
        override val vcsCommitHash = data["vcs.commitHash"] ?: ""
        override val vcsCommitMessage = data["vcs.commitMessage"] ?: ""
        override val vcsCommitTime = data["vcs.commitTime"] ?: ""
        override val vcsTags = (data["vcs.tags"] ?: "").split(",").map { it.trim() }
        override val vcsLastTag = data["vcs.lastTag"] ?: ""
        override val vcsLastTagDiff = data["vcs.lastTagDiff"]?.toIntOrNull() ?: 0
        override val vcsDirty = data["vcs.dirty"]?.toBoolean() ?: false
        override val vcsCommitCount = data["vcs.commitCount"]?.toIntOrNull() ?: 0
    }.observeConst()
}

actual fun getRuntimeInformation(): List<Pair<String, ObservableValue<Any>>> {
    return emptyList()
}
