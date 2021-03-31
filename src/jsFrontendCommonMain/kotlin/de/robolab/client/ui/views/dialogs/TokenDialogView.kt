package de.robolab.client.ui.views.dialogs

import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.app.viewmodel.dialog.TokenDialogViewModel
import de.robolab.client.net.requests.auth.getTokenLinkPair
import de.robolab.client.net.sendHttpRequest
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.views.utils.buttonGroup
import de.robolab.client.utils.electron
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.RESTRequestCodeException
import de.robolab.common.utils.Logger
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.button
import de.westermann.kwebview.components.link
import de.westermann.kwebview.components.textView
import kotlinx.browser.window
import kotlinx.coroutines.*

class TokenDialogView(viewModel: TokenDialogViewModel) : ViewCollection<View>() {

    private var success = false
    private var isOpen = true

    private val logger = Logger(this)

    private fun open(url: String): Boolean {
        val electron = electron

        return if (electron == null) {
            window.open(url) != null
        } else {
            electron.openExternal(url)
            true
        }
    }

    init {
        val contentTab = boxView {
            textView("Please wait...")
        }

        viewModel.onClose {
            isOpen = false
            viewModel.onFinish(success)
        }

        GlobalScope.launch {
            val tokenLinkPair = viewModel.server.getTokenLinkPair().okOrNull()
            success = false

            if (tokenLinkPair != null) {
                if (!open(tokenLinkPair.loginURL)) {
                    contentTab.apply {
                        contentTab.clear()
                        classList += "token-popup"
                        textView("The browser has blocked the OAuth page. Please open the OAuth page manually or allow this popup in your browsers settings.")
                        link(tokenLinkPair.loginURL) {
                            buttonGroup(true) {
                                button("Open OAuth page")
                            }
                            this.html.target = "_blank"
                        }
                    }
                }

                delay(1000)

                while (isOpen) {
                    val r = tokenLinkPair.sendHttpRequest().also {
                        it.ifErr { e ->
                            if (e !is RESTRequestCodeException || e.code != HttpStatusCode.NoContent) {
                                logger.warn {
                                    e.toString()
                                }
                            }
                        }
                    }.okOrNull()
                    if (r != null) {
                        withContext(Dispatchers.Main) {
                            viewModel.server.authHeader = r.tokenHeader
                            success = true
                            viewModel.close()
                        }
                        break
                    } else {
                        delay(1000)
                    }
                }
            }
        }
    }

    companion object : ViewFactory {
        override fun matches(viewModel: ViewModel): Boolean {
            return viewModel is TokenDialogViewModel
        }

        override fun create(viewModel: ViewModel): View {
            return TokenDialogView(viewModel as TokenDialogViewModel)
        }
    }
}
