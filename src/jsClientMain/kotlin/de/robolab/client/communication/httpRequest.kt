package de.robolab.client.communication

import de.robolab.common.utils.Logger
import kotlin.browser.window

actual fun httpRequest(url: String, onFinish: (String?) -> Unit) {
    window.fetch(url).then {
        it.text()
    }.then {
        onFinish(it)
    }.catch {
        Logger("httpRequest").warn(it)
        onFinish(null)
    }
}
