package de.westermann.kwebview.components

import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.property
import de.westermann.kwebview.KWebViewDsl
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.ViewForLabel
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener

class Checkbox(
        initValue: Boolean = false
) : ViewForLabel() {

    fun bind(property: ObservableValue<Boolean>) {
        checkedProperty.bind(property)
        readonly = true
    }

    fun bind(property: ObservableProperty<Boolean>) {
        checkedProperty.bindBidirectional(property)
    }

    fun unbind() {
        checkedProperty.unbind()
    }

    var checked: Boolean
        get() = html.checked
        set(value) {
            html.checked = value
            checkedProperty.invalidate()
        }

    val checkedProperty: ObservableProperty<Boolean> = property(this::checked)

    val disabledProperty = property(html::disabled)

    init {
        checked = initValue
        html.type = "checkbox"

        var lastValue = checked
        val changeListener = object : EventListener {
            override fun handleEvent(event: Event) {
                val value = checked
                if (value != checkedProperty.value || value != lastValue) {
                    lastValue = value
                    checkedProperty.value = value
                }
            }
        }

        html.addEventListener("change", changeListener)
        html.addEventListener("keyup", changeListener)
        html.addEventListener("keypress", changeListener)
    }
}

@KWebViewDsl
fun ViewCollection<in Checkbox>.checkbox(value: Boolean = false, init: Checkbox.() -> Unit = {}) =
        Checkbox(value).also(this::append).also(init)

@KWebViewDsl
fun ViewCollection<in Checkbox>.checkbox(value: ObservableValue<Boolean>, init: Checkbox.() -> Unit = {}) =
        Checkbox(value.value).also(this::append).also { it.bind(value) }.also(init)

@KWebViewDsl
fun ViewCollection<in Checkbox>.checkbox(value: ObservableProperty<Boolean>, init: Checkbox.() -> Unit = {}) =
        Checkbox(value.value).also(this::append).also { it.bind(value) }.also(init)
