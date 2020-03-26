package de.robolab.web.adapter

import org.w3c.dom.HTMLElement

external class Hammer(element: HTMLElement, options: dynamic) {
    fun on(name: String, callback: (HammerEvent) -> Unit)
    fun get(name: String): HammerOptions
}

fun Hammer.enablePan() {
    get("pan").set(object {
        val enable = true
        val direction = js("Hammer.DIRECTION_ALL")
    })
}

fun Hammer.enablePinch() {
    get("pinch").set(object {
        val enable = true
    })
}

fun Hammer.enableRotate() {
    get("rotate").set(object {
        val enable = true
    })
}

fun Hammer.onTap(callback: (HammerEvent) -> Unit) {
    on("tap", callback)
}

fun Hammer.onPress(callback: (HammerEvent) -> Unit) {
    on("press", callback)
}

fun Hammer.onPanStart(callback: (HammerEvent) -> Unit) {
    on("panstart", callback)
}

fun Hammer.onPanMove(callback: (HammerEvent) -> Unit) {
    on("panmove", callback)
}

fun Hammer.onPanEnd(callback: (HammerEvent) -> Unit) {
    on("panend", callback)
}

fun Hammer.onPinchStart(callback: (HammerEvent) -> Unit) {
    on("pinchstart", callback)
}

fun Hammer.onPinchMove(callback: (HammerEvent) -> Unit) {
    on("pinchmove", callback)
}

fun Hammer.onPinchEnd(callback: (HammerEvent) -> Unit) {
    on("pinchend", callback)
}

fun Hammer.onRotateStart(callback: (HammerEvent) -> Unit) {
    on("rotatestart", callback)
}

fun Hammer.onRotateMove(callback: (HammerEvent) -> Unit) {
    on("rotatemove", callback)
}

fun Hammer.onRotateEnd(callback: (HammerEvent) -> Unit) {
    on("rotateend", callback)
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

    fun preventDefault()
}

external interface HammerCenter {
    val x: Double
    val y: Double
}