package de.robolab.client.web.adapter

import de.westermann.kwebview.Document
import org.w3c.dom.TouchEvent
import org.w3c.dom.events.MouseEvent


@Suppress("unused")
fun Hammer.enablePan() {
    get("pan").set(object {
        val enable = true
        val direction = js("Hammer.DIRECTION_ALL")
        val threshold = 0
    })
}

@Suppress("unused")
fun Hammer.enablePinch() {
    get("pinch").set(object {
        val enable = true
    })
}

@Suppress("unused")
fun Hammer.enablePress() {
    get("press").set(object {
        val enable = true
        val threshold = 1
        val time = 500
    })
}

@Suppress("unused")
fun Hammer.enableTap() {
    get("tap").set(object {
        val enable = true
        val threshold = 5
        val posThreshold = 15
    })
}

@Suppress("unused")
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


fun HammerEvent.isTouch() = pointerType == "touch" || pointerType == "pen"
fun HammerEvent.isMouse() = pointerType == "mouse"

@Suppress("DuplicatedCode")
val HammerEvent.ctrlKey: Boolean
    get() {
        var key = false

        val mouseEvent = srcEvent as? MouseEvent
        if (mouseEvent != null) {
            key = mouseEvent.ctrlKey
        }
        if (Document.isTouchSupported) {
            val touchEvent = srcEvent as? TouchEvent
            if (touchEvent != null) {
                key = touchEvent.ctrlKey
            }
        }

        return key
    }

@Suppress("DuplicatedCode")
val HammerEvent.altKey: Boolean
    get() {
        var key = false

        val mouseEvent = srcEvent as? MouseEvent
        if (mouseEvent != null) {
            key = mouseEvent.altKey
        }
        if (Document.isTouchSupported) {
            val touchEvent = srcEvent as? TouchEvent
            if (touchEvent != null) {
                key = touchEvent.altKey
            }
        }

        return key
    }

@Suppress("DuplicatedCode")
val HammerEvent.shiftKey: Boolean
    get() {
        var key = false

        val mouseEvent = srcEvent as? MouseEvent
        if (mouseEvent != null) {
            key = mouseEvent.shiftKey
        }
        if (Document.isTouchSupported) {
            val touchEvent = srcEvent as? TouchEvent
            if (touchEvent != null) {
                key = touchEvent.shiftKey
            }
        }

        return key
    }
