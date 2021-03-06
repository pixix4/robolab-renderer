package de.westermann.kwebview.components

import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.mapMutableBinding
import de.westermann.kobserve.property.property
import de.westermann.kwebview.KWebViewDsl
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.createHtmlView
import org.w3c.dom.HTMLSpanElement

/**
 * Represents a html span element.
 *
 * @author lars
 */
class TextView(
    value: Any = ""
) : View(createHtmlView<HTMLSpanElement>()) {

    override val html = super.html as HTMLSpanElement

    fun bind(property: ObservableValue<Any>) {
        textProperty.bind(property.mapBinding { it.toString() })
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

    val textProperty = property(this::text)

    init {
        text = value.toString()
    }
}

@KWebViewDsl
fun ViewCollection<in TextView>.textView(text: String = "", init: TextView.() -> Unit = {}) =
    TextView(text).also(this::append).also(init)

@KWebViewDsl
fun ViewCollection<in TextView>.textView(text: ObservableValue<Any>, init: TextView.() -> Unit = {}) =
    TextView(text.value).also(this::append).also { it.bind(text) }.also(init)
