package de.robolab.communication

import kotlin.browser.window

actual fun httpRequest(url: String, onFinish: (String?) -> Unit) {
    window.fetch(url).then {
        it.text()
    }.then {
        onFinish(it)
    }.catch {
        onFinish(null)
    }
}
