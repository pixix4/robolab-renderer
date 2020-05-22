@file:JsModule("events")
@file:JsNonModule
package de.robolab.server.externaljs.events

import org.w3c.dom.events.Event
import kotlin.js.Promise

external interface EventEmitter{
    val newListener: Event
    val removeListener: Event
    fun<T> addListener(eventName:String, callback: T)
    fun emit(eventName: String, vararg args:dynamic) : Boolean
    fun eventNames(): Array<Any>
    fun getMaxListeners(): Int
    fun listenerCount(eventName:String) :Int
    fun<T> listeners(eventName: String):Array<T>
    fun<T> off(eventName: String, listener: T): EventEmitter
    fun<T> on(eventName: String, listener: T): EventEmitter
    fun<T> once(eventName: String, listener: T): EventEmitter
    fun<T> prependListener(eventName: String, listener: T): EventEmitter
    fun<T> prependOnceListener(eventName: String, listener: T): EventEmitter
    fun removeAllListeners(): EventEmitter
    fun removeAllListeners(eventName: String): EventEmitter
    fun<T> removeListener(eventName: String, listener: T): EventEmitter
    fun setMaxListeners(n:Int): EventEmitter
    fun<T> rawListeners(eventName:String):Array<T>
}

external fun<T> once(emitter: EventEmitter, eventName: String) : Promise<T>