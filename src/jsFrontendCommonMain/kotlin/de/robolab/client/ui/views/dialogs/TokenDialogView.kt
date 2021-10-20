package de.robolab.client.ui.views.dialogs

import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.app.viewmodel.dialog.TokenDialogViewModel
import de.robolab.client.net.requests.auth.DeviceAuthPrompt
import de.robolab.client.net.requests.auth.IDeviceAuthPromptCallbacks
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.views.utils.buttonGroup
import de.robolab.client.utils.electron
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
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

    private fun open(url: String): Boolean {
        val electron = electron

        return if (electron == null) {
            window.open(url) != null
        } else {
            electron.openExternal(url)
            true
        }
    }

    val deviceAuthPrompt = property<DeviceAuthPrompt>()

    init {
        val contentTab = boxView {
            textView("Please wait...")
        }

        viewModel.onClose {
            isOpen = false
            viewModel.onFinish(success)
        }

        GlobalScope.launch {
            viewModel.server.performDeviceAuth {
                deviceAuthPrompt.value = it
                object : IDeviceAuthPromptCallbacks {
                    override fun onPromptSuccess() {
                        viewModel.close()
                    }

                    override fun onPromptError() {
                        viewModel.close()
                    }

                    override fun onPromptRefresh(newPrompt: DeviceAuthPrompt) {
                        deviceAuthPrompt.value = newPrompt
                    }
                }
            }
        }

        val uriProperty = deviceAuthPrompt.mapBinding { it?.verificationURI }
        val userCodeProperty = deviceAuthPrompt.mapBinding { it?.userCode ?: "" }

        uriProperty.onChange {
            val uri = uriProperty.value

            if (uri != null) {
                if (!open(uri)) {
                    contentTab.apply {
                        contentTab.clear()
                        classList += "token-popup"
                        textView("The browser has blocked the OAuth page. Please open the OAuth page manually or allow this popup in your browsers settings.")
                        textView(userCodeProperty)
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
