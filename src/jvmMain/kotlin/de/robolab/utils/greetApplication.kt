package de.robolab.utils

internal actual fun ConsoleGreeter.greetApplication(appLogo: String, appCreators: String) {
    println("$appLogo\n$appCreators")
}