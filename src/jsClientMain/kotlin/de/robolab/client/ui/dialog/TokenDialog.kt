package de.robolab.client.ui.dialog

import de.robolab.client.net.IRobolabServer
import de.robolab.client.net.requests.getTokenLinkPair
import de.robolab.client.net.sendHttpRequest
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.RESTRequestCodeError
import de.robolab.common.utils.Logger
import de.westermann.kobserve.event.emit
import de.westermann.kwebview.components.button
import de.westermann.kwebview.components.link
import de.westermann.kwebview.components.textView
import kotlinx.browser.window
import kotlinx.coroutines.*

class TokenDialog private constructor(
    private val server: IRobolabServer,
    private val userConfirm: Boolean,
    private val onFinish: (Boolean) -> Unit
) : Dialog("Token") {

    private var success = false
    private var isOpen = true

    private val logger = Logger(this)

    init {
        val contentTab = tab {

        }

        onClose {
            isOpen = false
            onFinish(success)
        }

        GlobalScope.launch {
            val tokenLinkPair = server.getTokenLinkPair().okOrNull()
            success = false

            if (tokenLinkPair != null) {
                if (window.open(tokenLinkPair.loginURL) == null) {
                    contentTab.apply {
                        classList += "token-popup"
                        textView("The browser has blocked the OAuth page. Please open the OAuth page manually or allow this popup in your browsers settings.")
                        link(tokenLinkPair.loginURL) {
                            button("Open OAuth page")
                            this.html.target = "_blank"
                        }
                    }
                }

                delay(1000)

                while (isOpen) {
                    val r = tokenLinkPair.sendHttpRequest().also {
                        it.ifErr { e ->
                            if (e !is RESTRequestCodeError || e.code != HttpStatusCode.NoContent) {
                                logger.warn(e.toString())
                            }
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
