package de.westermann.kwebview.components

import de.robolab.client.app.model.base.MaterialIcon
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.property
import de.westermann.kwebview.KWebViewDsl
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.createHtmlView
import kotlinx.browser.document
import org.w3c.dom.HTMLSpanElement
import kotlinx.dom.clear

/**
 * Represents all kinds of icon views.
 *
 * @author lars
 */
class IconView(icon: MaterialIcon?) : View(createHtmlView<HTMLSpanElement>()) {

    override val html = super.html as HTMLSpanElement

    fun bind(property: ObservableValue<MaterialIcon?>) {
        iconProperty.bind(property)
    }

    fun unbind() {
        iconProperty.unbind()
    }

    var icon: MaterialIcon? = null
        set(value) {
            field = value
            html.clear()
            value?.let {
                val element = document.createElement("i").apply {
                    classList.add("material-icons")
                    textContent = it.ligature
                }
                html.appendChild(element)
            }
            iconProperty.invalidate()
        }

    val iconProperty: ObservableProperty<MaterialIcon?> = property(this::icon)

    init {
        this.icon = icon
    }
}

@KWebViewDsl
fun ViewCollection<in IconView>.iconView(icon: MaterialIcon? = null, init: IconView.() -> Unit = {}) =
        IconView(icon).also(this::append).also(init)

@KWebViewDsl
fun ViewCollection<in IconView>.iconView(icon: ObservableValue<MaterialIcon?>, init: IconView.() -> Unit = {}) =
        IconView(icon.value).also(this::append).also { it.bind(icon) }.also(init)
