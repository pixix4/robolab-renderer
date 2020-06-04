package de.westermann.kwebview

import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Point
import de.robolab.common.utils.Rectangle
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.property
import org.w3c.dom.DOMRect
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.*
import kotlin.browser.document

inline fun <reified V : HTMLElement> createHtmlView(tag: String? = null): V {
    var tagName: String
    if (tag != null) {
        tagName = tag
    } else {
        tagName = V::class.js.name.toLowerCase().replace("html([a-z]*)element".toRegex(), "$1")
        if (tagName.isBlank()) {
            tagName = "div"
        }
    }
    return document.createElement(tagName) as V
}

fun String.toDashCase() = replace("([a-z])([A-Z])".toRegex(), "$1-$2").toLowerCase()

inline fun <reified T> EventHandler<T>.bind(element: EventTarget, event: String) {
    val listener = object : EventListener {
        override fun handleEvent(event: Event) {
            this@bind.emit(event as T)
        }
    }
    var isAttached = false

    val updateState = {
        if (isEmpty() && isAttached) {
            element.removeEventListener(event, listener)
            isAttached = false
        } else if (!isEmpty() && !isAttached) {
            element.addEventListener(event, listener)
            isAttached = true
        }
    }

    onAttach = updateState
    onDetach = updateState
    updateState()
}

fun DOMRect.toRectangle(): Rectangle = Rectangle(x, y, width, height)
val MouseEvent.clientPosition get() = Point(clientX, clientY)

external fun delete(p: dynamic): Boolean = definedExternally

fun delete(thing: dynamic, key: String) {
    delete(thing[key])
}

fun <V : View> ViewCollection<V>.bindView(vararg properties: ObservableValue<*>, block: () -> V): ObservableValue<V> {
    val viewProperty = property(block())
    var view by viewProperty
    +view

    fun change() {
        val new = block()
        if (new != view) {
            replace(view, new)
            view = new
        }
    }
    for (p in properties) {
        p.onChange { change() }
    }

    return viewProperty
}

val KeyboardEvent.modifierKey: Boolean
    get() = altKey || ctrlKey || shiftKey