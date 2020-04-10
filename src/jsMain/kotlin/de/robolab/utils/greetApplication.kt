package de.robolab.utils

internal actual fun ConsoleGreeter.greetApplication(appLogo: String, appCreators: String) {
    console.log("$appLogo\n%c$appCreators", "color: gray")
}