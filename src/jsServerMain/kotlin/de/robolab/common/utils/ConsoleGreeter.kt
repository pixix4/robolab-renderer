package de.robolab.common.utils

internal actual fun ConsoleGreeter.greetApplication(appLogo: String, appCreators: String) {
    console.log("$appLogo\n$appCreators")
}
