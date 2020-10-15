package de.robolab.client.ui.dialog

import de.robolab.client.net.IRobolabServer
import de.robolab.client.net.requests.getTokenLinkPair
import de.robolab.client.net.sendHttpRequest
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.RESTRequestCodeException
import de.robolab.common.utils.Logger
import javafx.scene.layout.Priority
import kotlinx.coroutines.*
import tornadofx.vbox
import tornadofx.vgrow
import java.awt.Desktop
import java.net.URI


class TokenDialog : GenericDialog() {

    private val server: IRobolabServer by param()
    private val userConfirm: Boolean by param()
    private val onFinish: (Boolean) -> Unit by param()
    private val logger = Logger(this)

    override val root = buildContent("Token") {
        vgrow = Priority.ALWAYS
        vbox {
            vgrow = Priority.ALWAYS


        }
    }

    private var success = false
    override fun onBeforeShow() {
        super.onBeforeShow()

        GlobalScope.launch {
            val tokenLinkPair = server.getTokenLinkPair().okOrNull()
            success = false

            if (tokenLinkPair != null) {
                openUrlInBrowser(tokenLinkPair.loginURL)

                delay(1000)

                while (isDocked) {
                    val r = tokenLinkPair.sendHttpRequest().also {
                        it.ifErr { e ->
                            if (e !is RESTRequestCodeException || e.code != HttpStatusCode.NoContent) {
                                logger.warn(e.toString())
                            }
                        }
                    }.okOrNull()
                    if (r != null) {
                        withContext(Dispatchers.Main) {
                            server.authHeader = r.tokenHeader
                            close()
                        }
                        break
                    } else {
                        delay(1000)
                    }
                }
            }
        }
    }

    override fun onUndock() {
        super.onUndock()

        onFinish(success)
    }

    companion object {
        val browsers = arrayOf(
            "xdg-open",
            "google-chrome",
            "firefox",
            "opera",
            "konqueror",
            "mozilla"
        )

        fun openUrlInBrowser(uri: String) {
            val osName = System.getProperty("os.name")
            try {
                if (osName.startsWith("Mac OS")) {
                    Desktop.getDesktop().browse(URI.create(uri))
                } else if (osName.startsWith("Windows")) {
                    Runtime.getRuntime().exec(
                        "rundll32 url.dll,FileProtocolHandler $uri"
                    )
                } else { //assume Unix or Linux
                    var browser: String? = null
                    for (b in browsers) {
                        if (browser == null && Runtime.getRuntime()
                                .exec(arrayOf("which", b)).inputStream.read() != -1
                        ) {
                            Runtime.getRuntime().exec(arrayOf(b.also { browser = it }, uri))
                        }
                    }
                    if (browser == null) {
                        throw Exception("No web browser found")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun open(server: IRobolabServer, userConfirm: Boolean, onFinish: (Boolean) -> Unit) {
            open<TokenDialog>(
                "server" to server,
                "userConfirm" to userConfirm,
                "onFinish" to onFinish
            )
        }
    }
}
