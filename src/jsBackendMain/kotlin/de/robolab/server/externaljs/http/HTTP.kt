@file:JsModule("http")
@file:JsNonModule

package de.robolab.server.externaljs.http

import de.robolab.server.externaljs.JSArray
import de.robolab.server.externaljs.stream.Readable
import de.robolab.server.externaljs.stream.Writable

external fun createServer(app: dynamic): dynamic

external interface IncomingMessage : Readable {
    val aborted: Boolean
    val complete: Boolean

    fun destroy()
    fun destroy(error: Error)

    val headers: dynamic

    val httpVersion: String

    val rawHeaders: JSArray<String>

    val rawTrailers: JSArray<String>

    fun setTimeout(msecs: Int): IncomingMessage
    fun setTimeout(msecs: Int, callback: () -> Unit): IncomingMessage

    val trailers: dynamic
}

external interface IncomingServerMessage : IncomingMessage {
    val method: String
    val url: String
}

external interface ServerResponse : Writable {
    fun addTrailers(headers: dynamic)

    fun flushHeaders()

    fun getHeader(name: String): Any?

    fun getHeaderNames(): JSArray<String>

    fun getHeaders(): dynamic

    fun hasHeader(name: String): Boolean

    val headersSent: Boolean

    fun removeHeader(name: String)

    var sendDate: Boolean

    fun setHeader(name: String, value: dynamic)

    fun setTimeout(msecs: Int): ServerResponse
    fun setTimeout(msecs: Int, callback: () -> Unit): ServerResponse

    var statusCode: Int

    var statusMessage: String

    fun writeContinue()

    fun writeHead(statusCode: Int): ServerResponse
    fun writeHead(statusCode: Int, statusMessage: String): ServerResponse
    fun writeHead(statusCode: Int, headers: dynamic): ServerResponse
    fun writeHead(statusCode: Int, statusMessage: String, headers: dynamic): ServerResponse

    fun writeProcessing()
}