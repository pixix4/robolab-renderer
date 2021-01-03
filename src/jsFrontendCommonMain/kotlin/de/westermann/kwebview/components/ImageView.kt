package de.westermann.kwebview.components

import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.property
import de.westermann.kwebview.*
import org.w3c.dom.HTMLImageElement

class ImageView(
        src: String
) : View(createHtmlView<HTMLImageElement>("img")) {

    override val html = super.html as HTMLImageElement

    fun bind(property: ObservableValue<String>) {
        sourceProperty.bind(property)
    }

    fun unbind() {
        sourceProperty.unbind()
    }

    var source: String
        get() = html.src
        set(value) {
            html.src = value
            sourceProperty.invalidate()
        }

    val sourceProperty: ObservableProperty<String> = property(this::source)


    var alt by AttributeDelegate("alt")

    init {
        source = src
    }
}

@KWebViewDsl
fun ViewCollection<in ImageView>.imageView(src: String = "", init: ImageView.() -> Unit = {}) =
        ImageView(src).also(this::append).also(init)

@KWebViewDsl
fun ViewCollection<in ImageView>.imageView(src: ObservableValue<String>, init: ImageView.() -> Unit = {}) =
        ImageView(src.value).also(this::append).also { it.bind(src) }.also(init)
