package de.robolab.client.ui.views.dialogs

import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.app.viewmodel.dialog.TokenDialogViewModel
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.views.utils.buttonGroup
import de.robolab.client.utils.electron
import de.westermann.kobserve.event.now
import de.westermann.kobserve.property.mapBinding
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.button
import de.westermann.kwebview.components.link
import de.westermann.kwebview.components.textView
import kotlinx.browser.window

class TokenDialogView(viewModel: TokenDialogViewModel) : ViewCollection<View>() {

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

        val uriProperty = viewModel.deviceAuthPrompt.mapBinding {
            it.verificationURIComplete ?: it.verificationURI
        }
        val userCodeProperty = viewModel.deviceAuthPrompt.mapBinding { it.userCode }

        uriProperty.onChange.now {
            val uri = uriProperty.value

            if (!open(uri)) {
                contentTab.apply {
                    contentTab.clear()
                    classList += "token-popup"
                    textView("The browser has blocked the OAuth page. Please open the OAuth page manually or allow this popup in your browsers settings.")
                    textView(userCodeProperty)
                    boxView("form-content-button-view") {
                        link(uri) {
                            buttonGroup(true) {
                                button("Open OAuth page")
                            }
                            this.html.target = "_blank"
                        }
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
