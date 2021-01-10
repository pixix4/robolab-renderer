package de.robolab.client

import de.robolab.client.app.controller.MainController
import de.robolab.client.ui.initMainView
import kotlinx.browser.window

fun main() {
    val argMap = window.location.search.drop(1).split("&").associate {
        val s = it.split("=")
        s[0] to (s.getOrNull(1) ?: "")
    }

    val args = MainController.Args(
        argMap["layout"],
        argMap["groups"],
        argMap["fullscreen"]?.let { "true" },
        argMap["connect"]?.let { "true" },
    )

    initMainView(args)
}
