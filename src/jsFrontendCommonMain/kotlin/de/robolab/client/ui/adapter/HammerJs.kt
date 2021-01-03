package de.robolab.client.ui.adapter

import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event

@JsModule("hammerjs")
external class Hammer(element: HTMLElement, options: dynamic) {
    fun on(name: String, callback: (HammerEvent) -> Unit)
    fun get(name: String): HammerOptions
}

external interface HammerOptions {
    fun set(options: dynamic)
}

external interface HammerEvent {
    val center: HammerCenter
    val rotation: Double
    val scale: Double
    val deltaX: Double
    val deltaY: Double

    val type: String
    val tapCount: Int
    val pointerType: String

    val srcEvent: Event

    fun preventDefault()
}

external interface HammerCenter {
    val x: Double
    val y: Double
}

open external class GestureEvent: Event {
    val altKey: Boolean
    val cancelBubble: Boolean
    val clientX: Int
    val clientY: Int
    val ctrlKey: Boolean
    val detail: Int
    val layerX: Int
    val layerY: Int
    val metaKey: Boolean
    val pageX: Int
    val pageY: Int
    val returnValue: Boolean
    val rotation: Double
    val scale: Double
    val screenX: Int
    val screenY: Int
    val shiftKey: Boolean
    val srcElement: HTMLElement
    val which: Int
}
