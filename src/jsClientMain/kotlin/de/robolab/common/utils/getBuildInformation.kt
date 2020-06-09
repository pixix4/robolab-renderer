package de.robolab.common.utils

import de.robolab.client.net.http
import de.robolab.client.net.web
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.constObservable
import kotlin.browser.window

actual fun getBuildInformation(): String? {
    return null
}

actual suspend fun getAsyncBuildInformation(): String {
    return http {
        web("build.ini")
    }.exec().body ?: ""
}

private fun getOsName(): String {
    val oscpu = window.navigator.oscpu

    return when {
        oscpu.matches("Win16") -> "Windows 3.11"
        oscpu.matches("(Windows 95)|(Win95)|(Windows_95)") -> "Windows 95"
        oscpu.matches("(Windows 98)|(Win98)") -> "Windows 98"
        oscpu.matches("(Windows NT 5.0)|(Windows 2000)") -> "Windows 2000"
        oscpu.matches("(Windows NT 5.1)|(Windows XP)") -> "Windows XP"
        oscpu.matches("(Windows NT 5.2)") -> "Windows Server 2003"
        oscpu.matches("(Windows NT 6.0)") -> "Windows Vista"
        oscpu.matches("(Windows NT 6.1)") -> "Windows 7"
        oscpu.matches("(Windows NT 6.2)|(WOW64)") -> "Windows 8"
        oscpu.matches("(Windows 10.0)|(Windows NT 10.0)") -> "Windows 10"
        oscpu.matches("(Windows NT 4.0)|(WinNT4.0)|(WinNT)|(Windows NT)") -> "Windows NT 4.0"
        oscpu.matches("Windows ME") -> "Windows ME"
        oscpu.matches("OpenBSD") -> "Open BSD"
        oscpu.matches("SunOS") -> "Sun OS"
        oscpu.matches("(Linux)|(X11)") -> "Linux"
        oscpu.matches("(Mac_PowerPC)|(Macintosh)") -> "Mac OS"
        oscpu.matches("QNX") -> "QNX"
        oscpu.matches("BeOS") -> "BeOS"
        oscpu.matches("OS/2") -> "OS/2"
        oscpu.matches("(nuhk)|(Googlebot)|(Yammybot)|(Openbot)|(Slurp)|(MSNBot)|(Ask Jeeves/Teoma)|(ia_archiver)") -> "Search Bot"
        else -> "Unknown"
    } + " ($oscpu)"


}

actual fun getRuntimeInformation(): List<Pair<String, ObservableValue<Any>>> {
    return listOf(
        "UserAgent" to constObservable(window.navigator.userAgent),
        "Platform" to constObservable(window.navigator.platform),
        "Os" to constObservable(getOsName()),
        "Threads" to constObservable(window.navigator.hardwareConcurrency)
    )
}
