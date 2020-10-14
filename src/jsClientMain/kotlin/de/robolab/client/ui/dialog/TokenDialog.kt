package de.robolab.client.ui.dialog

import de.robolab.client.net.IRobolabServer
import de.robolab.client.net.requests.getTokenLinkPair
import de.robolab.client.net.sendHttpRequest
import de.westermann.kobserve.event.emit
import kotlinx.browser.window
import kotlinx.coroutines.*

class TokenDialog private constructor(
    private val server: IRobolabServer,
    private val userConfirm: Boolean,
    private val onFinish: (Boolean) -> Unit
) : Dialog("Token") {

    private var success = false
    private var isOpen = true

    init {
        tab {

        }

        onClose {
            isOpen = false
            onFinish(success)
        }

        GlobalScope.launch {
            val tokenLinkPair = server.getTokenLinkPair().okOrNull()
            success = false

            if (tokenLinkPair != null) {
                window.open(tokenLinkPair.loginURL)

                delay(1000)

                while (isOpen) {
                    val r = tokenLinkPair.sendHttpRequest().also {
                        it.ifErr { e ->
                            e.printStackTrace()
                        }
                    }.okOrNull()
                    if (r != null) {
                        withContext(Dispatchers.Main) {
                            server.authHeader = r.tokenHeader
                            onClose.emit()
                        }
                        break
                    } else {
                        delay(1000)
                    }
                }
            }
        }
    }

    companion object {
        fun open(server: IRobolabServer, userConfirm: Boolean, onFinish: (Boolean) -> Unit) {
            open(TokenDialog(server, userConfirm, onFinish))
        }
    }
}
