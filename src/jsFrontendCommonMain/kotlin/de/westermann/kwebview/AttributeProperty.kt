package de.westermann.kwebview

import de.westermann.kobserve.Binding
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.event.EventHandler
import org.w3c.dom.HTMLElement

class AttributeProperty(
    private val container: View,
    private val paramName: String
): ObservableProperty<String?> {

    override fun get(): String? {
        return container.html.getAttribute(paramName)
    }

    override fun set(value: String?) {
        if (value == null) {
            container.html.removeAttribute(paramName)
        } else {
            container.html.setAttribute(paramName, value.toString())
        }
    }

    override val onChange = EventHandler<Unit>()
    override var binding: Binding<String?> = Binding.Unbound()
}
