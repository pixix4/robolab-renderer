package de.westermann.kwebview

import de.robolab.common.utils.Vector
import de.robolab.common.utils.Rectangle
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.now
import org.w3c.dom.DOMRect
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.*
import kotlinx.browser.document
import kotlin.math.max

inline fun <reified V : HTMLElement> createHtmlView(tag: String? = null): V {
    var tagName: String
    if (tag != null) {
        tagName = tag
    } else {
        tagName = V::class.js.name.lowercase().replace("html([a-z]*)element".toRegex(), "$1")
        if (tagName.isBlank()) {
            tagName = "div"
        }
    }
    return document.createElement(tagName) as V
}

fun String.toDashCase() = replace("([a-z])([A-Z])".toRegex(), "$1-$2").lowercase()

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
val MouseEvent.clientPosition get() = Vector(clientX, clientY)

external fun delete(p: dynamic): Boolean = definedExternally

fun delete(thing: dynamic, key: String) {
    delete(thing[key])
}

fun <T, V : View> ViewCollection<V>.bindView(property: ObservableValue<T>, block: (T) -> V) {
    var value = property.value
    var view = block(value)
    +view

    property.onChange {
        val newValue = property.value

        if (value != newValue) {
            value = newValue
            val newView = block(newValue)
            if (newView != view) {
                replace(view, newView)
                view = newView
            }
        }
    }
}

val KeyboardEvent.modifierKey: Boolean
    get() = altKey || ctrlKey || shiftKey

fun View.bindStyleProperty(name: String, property: ObservableValue<String>) {
    html.style.setProperty(name, property.value)
    property.onChange {
        html.style.setProperty(name, property.value)
    }
}

fun <TView : View, TItem : Any> ViewCollection<in TView>.sync(
    list: List<TItem>,
    create: (TItem) -> TView,
    update: (TView, TItem) -> Unit,
    delete: (TView) -> Unit,
) {
    for (i in 0 until max(this.size, list.size)) {
        val view = this.getOrNull(i) as? TView
        val item = list.getOrNull(i)

        if (item == null) {
            if (view == null) {
                TODO("Sync ViewCollection with List: both Collections returned null")
            } else {
                delete(view)
            }
        } else {
            if (view == null) {
                add(create(item))
            } else {
                update(view, item)
            }
        }
    }
}

fun <TView : View, TItem : Any> ViewCollection<in TView>.sync(
    listProperty: ObservableValue<List<TItem>>,
    create: (TItem) -> TView,
    update: (TView, TItem) -> Unit,
    delete: (TView) -> Unit,
) {
    listProperty.onChange.now {
        sync(listProperty.value, create, update, delete)
    }
}
