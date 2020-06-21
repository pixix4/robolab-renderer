package de.robolab.client.ui.style

import de.robolab.client.utils.PreferenceStorage
import tornadofx.FX
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler
import java.net.URLStreamHandlerFactory


object StylesheetLoader {

    private fun generateUrl(): String {
        return "internal:stylesheet-${PreferenceStorage.selectedTheme.name}.css"
    }

    private var lastUrl = ""

    fun load() {
        if (lastUrl.isNotEmpty()) {
            // Remove existing stylesheet url
            FX.stylesheets.remove(lastUrl)
        }

        // Update url if theme has changed
        lastUrl = generateUrl()
        FX.stylesheets.add(lastUrl)
    }

    private class InternalURLConnection(url: URL?) : URLConnection(url) {

        override fun connect() {}

        override fun getInputStream(): InputStream {
            // Convert `MainStyle` to css byte stream
            return MainStyle().render().byteInputStream()
        }
    }

    private class InternalURLStreamHandler : URLStreamHandler() {

        override fun openConnection(url: URL): URLConnection {
            // All `internal:[path]` urls serve the `MainStyle` stylesheet
            return InternalURLConnection(url)
        }
    }

    private class InternalURLStreamHandlerFactory : URLStreamHandlerFactory {

        val internalStreamHandler: URLStreamHandler = InternalURLStreamHandler()

        // Default css stream handler of TornadoFX
        // See tornadofx.CSS.kt
        val cssStreamHandler: URLStreamHandler = sun.net.www.protocol.css.Handler()

        override fun createURLStreamHandler(protocol: String): URLStreamHandler? {
            return when (protocol) {
                "css" -> cssStreamHandler
                "internal" -> internalStreamHandler
                else -> null
            }
        }
    }

    init {
        URL.setURLStreamHandlerFactory(InternalURLStreamHandlerFactory())
    }
}
