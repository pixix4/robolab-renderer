package de.robolab.common.utils

import de.robolab.client.net.http
import de.robolab.client.net.web
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.constObservable
import kotlinx.browser.window

actual fun getBuildInformation(): String? {
    return null
}

actual suspend fun getAsyncBuildInformation(): String {
    return http {
        web("build.ini")
    }.exec().body ?: ""
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
