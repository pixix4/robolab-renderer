@file:JsModule("stream")
@file:JsNonModule

package de.robolab.server.externaljs.stream

import de.robolab.server.externaljs.Buffer
import de.robolab.server.jsutils.JSDynErrorCallback
import de.robolab.server.externaljs.events.EventEmitter
import org.w3c.dom.ErrorEvent
import org.w3c.dom.events.Event

external interface Writable : EventEmitter {
    val close: Event

    val drain: Event

    val error: ErrorEvent

    val finish: Event

    val pipe: Event
    val unpipe: Event

    fun cork(): Unit
    fun uncork(): Unit

    fun destroy(): Writable
    fun destroy(error: Any?): Writable

    val destroyed: Boolean

    fun end(): Writable
    fun end(chunk: String): Writable
    fun end(chunk: String, encoding: String): Writable
    fun end(chunk: Buffer): Writable
    fun end(callback: JSDynErrorCallback): Writable
    fun end(chunk: String, callback: JSDynErrorCallback): Writable
    fun end(chunk: String, encoding: String, callback: JSDynErrorCallback): Writable
    fun end(chunk: Buffer, callback: JSDynErrorCallback): Writable

    fun setDefaultEncoding(encoding: String): Writable

    val writable: Boolean

    val writableEnded: Boolean

    val writableCorked: Int

    val writableFinished: Boolean

    val writableHighWaterMark: Int

    val writableLength: Int

    val writableObjectMode: Boolean

    fun write(chunk: String): Boolean
    fun write(chunk: String, encoding: String): Boolean
    fun write(chunk: Buffer): Boolean
    fun write(callback: JSDynErrorCallback): Boolean
    fun write(chunk: String, callback: JSDynErrorCallback): Boolean
    fun write(chunk: String, encoding: String, callback: JSDynErrorCallback): Boolean
    fun write(chunk: Buffer, callback: JSDynErrorCallback): Boolean
}

external interface Readable : EventEmitter {
    val readableFlowing: Boolean?
    val close: Event
    val data: Event

}