package de.robolab.common.utils

import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.observeConst
import kotlinx.browser.window

@Suppress("UnsafeCastFromDynamic")
actual fun getBuildInformation(): ObservableValue<IBuildInformation> {
    val versionFrontendString = js("VERSION_FRONTEND") as? String
    val versionBackendString = js("VERSION_BACKEND") as? String
    val buildTimeString = js("BUILD_TIME") as? String
    val buildJavaVersionString = js("BUILD_JAVA_VERSION") as? String
    val buildJavaVendorString = js("BUILD_JAVA_VENDOR") as? String
    val buildGradleVersionString = js("BUILD_GRADLE_VERSION") as? String
    val buildSystemNameString = js("BUILD_SYSTEM_NAME") as? String
    val buildSystemVersionString = js("BUILD_SYSTEM_VERSION") as? String
    val buildUserString = js("BUILD_USER") as? String
    val vcsBranchString = js("VCS_BRANCH") as? String
    val vcsCommitHashString = js("VCS_COMMIT_HASH") as? String
    val vcsCommitMessageString = js("VCS_COMMIT_MESSAGE") as? String
    val vcsCommitTimeString = js("VCS_COMMIT_TIME") as? String
    val vcsTagsString = js("VCS_TAGS") as? String
    val vcsLastTagString = js("VCS_LAST_TAG") as? String
    val vcsLastTagDiffString = js("VCS_LAST_TAG_DIFF") as? String
    val vcsDirtyString = js("VCS_DIRTY") as? String
    val vcsCommitCountString = js("VCS_COMMIT_COUNT") as? String

    return object : IBuildInformation {
        override val versionFrontend = versionFrontendString?.let { Version.parse(it) } ?: Version.UNKNOWN
        override val versionBackend = versionBackendString?.let { Version.parse(it) } ?: Version.UNKNOWN
        override val buildTime = buildTimeString ?: ""
        override val buildJavaVersion = buildJavaVersionString ?: ""
        override val buildJavaVendor = buildJavaVendorString ?: ""
        override val buildGradleVersion = buildGradleVersionString ?: ""
        override val buildSystemName = buildSystemNameString ?: ""
        override val buildSystemVersion = buildSystemVersionString ?: ""
        override val buildUser = buildUserString ?: ""
        override val vcsBranch = vcsBranchString ?: ""
        override val vcsCommitHash = vcsCommitHashString ?: ""
        override val vcsCommitMessage = vcsCommitMessageString ?: ""
        override val vcsCommitTime = vcsCommitTimeString ?: ""
        override val vcsTags = vcsTagsString?.split(",")?.map { it.trim() } ?: emptyList()
        override val vcsLastTag = vcsLastTagString ?: ""
        override val vcsLastTagDiff = vcsLastTagDiffString?.toIntOrNull() ?: 0
        override val vcsDirty = vcsDirtyString?.toBoolean() ?: false
        override val vcsCommitCount = vcsCommitCountString?.toIntOrNull() ?: 0
    }.observeConst()
}

private fun getPlatformName(): String {
    val platform = window.navigator.platform

    return when {
        platform.matches("Win16") -> "Windows 3.11"
        platform.matches("(Windows 95)|(Win95)|(Windows_95)") -> "Windows 95"
        platform.matches("(Windows 98)|(Win98)") -> "Windows 98"
        platform.matches("(Windows NT 5.0)|(Windows 2000)") -> "Windows 2000"
        platform.matches("(Windows NT 5.1)|(Windows XP)") -> "Windows XP"
        platform.matches("(Windows NT 5.2)") -> "Windows Server 2003"
        platform.matches("(Windows NT 6.0)") -> "Windows Vista"
        platform.matches("(Windows NT 6.1)") -> "Windows 7"
        platform.matches("(Windows NT 6.2)|(WOW64)") -> "Windows 8"
        platform.matches("(Windows 10.0)|(Windows NT 10.0)") -> "Windows 10"
        platform.matches("(Windows NT 4.0)|(WinNT4.0)|(WinNT)|(Windows NT)") -> "Windows NT 4.0"
        platform.matches("Windows ME") -> "Windows ME"
        platform.matches("OpenBSD") -> "Open BSD"
        platform.matches("SunOS") -> "Sun OS"
        platform.matches("(Linux)|(X11)") -> "Linux"
        platform.matches("(Mac_PowerPC)|(Macintosh)|(MacIntel)") -> "Mac OS"
        platform.matches("QNX") -> "QNX"
        platform.matches("BeOS") -> "BeOS"
        platform.matches("OS/2") -> "OS/2"
        platform.matches("(nuhk)|(Googlebot)|(Yammybot)|(Openbot)|(Slurp)|(MSNBot)|(Ask Jeeves/Teoma)|(ia_archiver)") -> "Search Bot"
        else -> "Unknown"
    } + " ($platform)"
}

actual fun getRuntimeInformation(): List<Pair<String, ObservableValue<Any>>> {
    return listOf(
        "UserAgent" to constObservable(window.navigator.userAgent),
        "Platform" to constObservable(getPlatformName())
    )
}
