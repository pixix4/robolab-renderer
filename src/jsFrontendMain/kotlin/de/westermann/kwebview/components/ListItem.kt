package de.westermann.kwebview.components

import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.property
import de.westermann.kwebview.KWebViewDsl
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.createHtmlView
import org.w3c.dom.HTMLLIElement

/**
 * Represents a html span element.
 *
 * @author lars
 */
class ListItem(
        value: String = ""
) : View(createHtmlView<HTMLLIElement>("li")) {

    override val html = super.html as HTMLLIElement

    fun bind(property: ObservableValue<String>) {
        textProperty.bind(property)
    }

    fun unbind() {
        textProperty.unbind()
    }

    var text: String
        get() = html.textContent ?: ""
        set(value) {
            html.textContent = value
            textProperty.invalidate()
        }

    val textProperty: ObservableProperty<String> = property(this::text)

    init {
        text = value
    }
}

@KWebViewDsl
fun ViewCollection<in ListItem>.listItem(text: String = "", init: ListItem.() -> Unit = {}) =
        ListItem(text).also(this::append).also(init)

@KWebViewDsl
fun ViewCollection<in ListItem>.listItem(text: ObservableValue<String>, init: ListItem.() -> Unit = {}) =
        ListItem(text.value).also(this::append).also { it.bind(text) }.also(init)
