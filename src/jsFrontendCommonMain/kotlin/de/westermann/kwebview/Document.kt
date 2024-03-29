package de.westermann.kwebview

import de.robolab.client.ui.adapter.GestureEvent
import de.westermann.kobserve.event.EventHandler
import org.w3c.dom.events.FocusEvent
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent
import kotlinx.browser.document

object Document {

    val onClick = EventHandler<MouseEvent>()
    val onDblClick = EventHandler<MouseEvent>()
    val onContext = EventHandler<MouseEvent>()

    val onMouseDown = EventHandler<MouseEvent>()
    val onMouseMove = EventHandler<MouseEvent>()
    val onMouseUp = EventHandler<MouseEvent>()
    val onMouseEnter = EventHandler<MouseEvent>()
    val onMouseLeave = EventHandler<MouseEvent>()

    val onWheel = EventHandler<WheelEvent>()

    val onKeyDown = EventHandler<KeyboardEvent>()
    val onKeyPress = EventHandler<KeyboardEvent>()
    val onKeyUp = EventHandler<KeyboardEvent>()

    val onFocus = EventHandler<FocusEvent>()
    val onBlur = EventHandler<FocusEvent>()

    val onGestureStart = EventHandler<GestureEvent>()
    val onGestureChange = EventHandler<GestureEvent>()
    val onGestureEnd = EventHandler<GestureEvent>()

    val isTouchSupported = js("'ontouchstart' in document.documentElement") == true

    init {
        onClick.bind(document, "click")
        onDblClick.bind(document, "dblclick")
        onContext.bind(document, "contextmenu")

        onMouseDown.bind(document, "mousedown")
        onMouseMove.bind(document, "mousemove")
        onMouseUp.bind(document, "mouseup")
        onMouseEnter.bind(document, "mouseenter")
        onMouseLeave.bind(document, "mouseleave")

        onWheel.bind(document, "wheel")

        onKeyDown.bind(document, "keydown")
        onKeyPress.bind(document, "keypress")
        onKeyUp.bind(document, "keyup")

        onFocus.bind(document, "focus")
        onBlur.bind(document, "blur")

        if (js("!!window.GestureEvent") == true) {
            onGestureStart.bind(document, "gesturestart")
            onGestureChange.bind(document, "gesturechange")
            onGestureEnd.bind(document, "gestureend")
        }
    }
}
