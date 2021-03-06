package de.westermann.kwebview.components

import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.property
import de.westermann.kwebview.AttributeDelegate
import de.westermann.kwebview.KWebViewDsl
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.createHtmlView
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener

class SelectView<T>(
        dataSet: List<T>,
        private val initValue: T,
        val transform: (T) -> String = { it.toString() }
) : ViewCollection<OptionView<T>>(createHtmlView<HTMLSelectElement>()) {

    override val html = super.html as HTMLSelectElement

    fun bind(property: ObservableValue<T>) {
        if (property is ObservableProperty) {
            valueProperty.bindBidirectional(property)
        } else {
            valueProperty.bind(property)
            readonly = true
        }
    }

    fun unbind() {
        valueProperty.unbind()
    }

    var dataSet: List<T> = emptyList()
        set(value) {
            field = value
            clear()

            value.forEachIndexed { index, v ->
                +OptionView(v).also { option ->
                    option.text = transform(v)
                    option.htmlValue = index.toString()
                }
            }
        }

    var index: Int
        get() = html.selectedIndex
        set(value) {
            val invalidate = html.selectedIndex != value
            html.selectedIndex = value
            if (invalidate) {
                valueProperty.invalidate()
            }
        }

    var value: T
        get() = dataSet.getOrNull(index) ?: initValue
        set(value) {
            index = dataSet.indexOf(value)
        }
    val valueProperty = property(this::value)

    private var readonlyInternal by AttributeDelegate("readonly")
    var readonly: Boolean
        get() = readonlyInternal != null
        set(value) {
            readonlyInternal = if (value) "readonly" else null
        }


    val disabledProperty = property(html::disabled)

    var tabindex by AttributeDelegate()
    fun preventTabStop() {
        tabindex = "-1"
    }

    init {
        this.dataSet = dataSet
        this.value = initValue

        html.addEventListener("change", object : EventListener {
            override fun handleEvent(event: Event) {
                valueProperty.invalidate()
            }
        })
    }
}

@KWebViewDsl
fun <T : Any> ViewCollection<in SelectView<T>>.selectView(dataSet: List<T>, initValue: T, transform: (T) -> String = { it.toString() }, init: SelectView<T>.() -> Unit = {}) =
        SelectView(dataSet, initValue, transform).also(this::append).also(init)

@KWebViewDsl
fun <T : Any> ViewCollection<in SelectView<T>>.selectView(dataSet: List<T>, property: ObservableValue<T>, transform: (T) -> String = { it.toString() }, init: SelectView<T>.() -> Unit = {}) =
        SelectView(dataSet, property.value, transform).apply { bind(property) }.also(this::append).also(init)


@KWebViewDsl
inline fun <reified T : Enum<T>> ViewCollection<in SelectView<T>>.selectView(initValue: T, noinline transform: (T) -> String = { it.toString() }, init: SelectView<T>.() -> Unit = {}) =
        SelectView(enumValues<T>().toList(), initValue, transform).also(this::append).also(init)

@KWebViewDsl
inline fun <reified T : Enum<T>> ViewCollection<in SelectView<T>>.selectView(property: ObservableValue<T>, noinline transform: (T) -> String = { it.toString() }, init: SelectView<T>.() -> Unit = {}) =
        SelectView(enumValues<T>().toList(), property.value, transform).apply { bind(property) }.also(this::append).also(init)

@KWebViewDsl
inline fun <reified T : Enum<T>> ViewCollection<in SelectView<T?>>.selectViewNullable(property: ObservableProperty<T?>, noinline transform: (T?) -> String = { it?.toString() ?: "" }, init: SelectView<T?>.() -> Unit = {}) =
        SelectView(enumValues<T>().toList(), property.value, transform).apply { bind(property) }.also(this::append).also(init)
