package de.robolab.jfx.style

import de.robolab.utils.PreferenceStorage
import tornadofx.*
import java.io.FileNotFoundException
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
            FX.stylesheets.remove(lastUrl)
        }
        lastUrl = generateUrl()
        FX.stylesheets.add(lastUrl)
    }

    private class InternalURLConnection(url: URL?) : URLConnection(url) {

        override fun connect() {
        }

        override fun getInputStream(): InputStream {
            return MainStyle().render().byteInputStream()
        }
    }
    
    private class InternalURLStreamHandler: URLStreamHandler() {

        override fun openConnection(url: URL): URLConnection {
            if (url.toString().toLowerCase().endsWith(".css")) {
                return InternalURLConnection(url)
            }
            throw FileNotFoundException()
        }
    }

    private class InternalURLStreamHandlerFactory : URLStreamHandlerFactory {

        val internalStreamHandler: URLStreamHandler = InternalURLStreamHandler()
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
