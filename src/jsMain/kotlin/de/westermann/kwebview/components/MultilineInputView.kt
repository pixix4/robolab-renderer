package de.westermann.kwebview.components

import de.westermann.kobserve.Property
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.ValidationProperty
import de.westermann.kobserve.not
import de.westermann.kobserve.property.property
import de.westermann.kwebview.*
import de.westermann.kwebview.ViewForLabel.Companion.generateId
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.KeyboardEvent
import kotlin.math.abs
import kotlin.random.Random

class MultilineInputView(
        type: InputType,
        initValue: String = ""
) : View(createHtmlView<HTMLTextAreaElement>()) {

    override val html = super.html as HTMLTextAreaElement

    private var label: Label? = null

    fun setLabel(label: Label) {
        if (this.label != null) {
            throw IllegalStateException("Label already set!")
        }

        this.label = label

        val id = id
        if (id?.isNotBlank() == true) {
            label.html.htmlFor = id
        } else {
            val newId = this::class.simpleName?.toDashCase() + "-" + generateId()
            this.id = newId
            label.html.htmlFor = newId
        }
    }

    private var requiredInternal by AttributeDelegate("required")
    var required: Boolean
        get() = requiredInternal != null
        set(value) {
            requiredInternal = if (value) "required" else null
        }
    private var readonlyInternal by AttributeDelegate("readonly")
    var readonly: Boolean
        get() = readonlyInternal != null
        set(value) {
            readonlyInternal = if (value) "readonly" else null
        }

    var tabindex by AttributeDelegate()
    fun preventTabStop() {
        tabindex = "-1"
    }

    fun bind(property: ReadOnlyProperty<String>) {
        valueProperty.bind(property)
        readonly = true
    }

    fun bind(property: Property<String>) {
        valueProperty.bindBidirectional(property)
    }

    fun bind(property: ValidationProperty<String>) {
        valueProperty.bindBidirectional(property)
        invalidProperty.bind(!property.validProperty)
    }

    fun unbind() {
        valueProperty.unbind()
        if (invalidProperty.isBound) {
            invalidProperty.unbind()
        }
    }

    var value: String
        get() = html.value
        set(value) {
            html.value = value
            valueProperty.invalidate()
        }

    val valueProperty: Property<String> = property(this::value)

    var placeholder: String
        get() = html.placeholder
        set(value) {
            html.placeholder = value
            placeholderProperty.invalidate()
        }

    val placeholderProperty: Property<String> = property(this::placeholder)

    val invalidProperty by ClassDelegate("invalid")
    var invalid by invalidProperty


    val disabledProperty = property(html::disabled)

    val selectionStart: Int?
        get() = html.selectionStart
    val selectionEnd: Int?
        get() = html.selectionEnd

    fun selectRange(start: Int, end: Int = start) = html.setSelectionRange(start, end)

    init {
        value = initValue

        var lastValue = value
        val changeListener = object : EventListener {
            override fun handleEvent(event: Event) {
                val value = value
                if (value != valueProperty.value || value != lastValue) {
                    lastValue = value
                    valueProperty.value = value
                }

                (event as? KeyboardEvent)?.let { e ->
                    when (e.keyCode) {
                        13, 27 -> blur()
                    }
                }
            }
        }

        html.addEventListener("change", changeListener)
        html.addEventListener("keyup", changeListener)
        html.addEventListener("keypress", changeListener)
    }
}


@KWebViewDsl
fun ViewCollection<in MultilineInputView>.multilineInputView(text: String = "", init: MultilineInputView.() -> Unit = {}) =
        MultilineInputView(InputType.TEXT, text).also(this::append).also(init)

@KWebViewDsl
fun ViewCollection<in MultilineInputView>.multilineInputView(text: ReadOnlyProperty<String>, init: MultilineInputView.() -> Unit = {}) =
        MultilineInputView(InputType.TEXT, text.value).also(this::append).also { it.bind(text) }.also(init)

@KWebViewDsl
fun ViewCollection<in MultilineInputView>.multilineInputView(text: Property<String>, init: MultilineInputView.() -> Unit = {}) =
        MultilineInputView(InputType.TEXT, text.value).also(this::append).also { it.bind(text) }.also(init)

@KWebViewDsl
fun ViewCollection<in MultilineInputView>.multilineInputView(text: ValidationProperty<String>, init: MultilineInputView.() -> Unit = {}) =
        MultilineInputView(InputType.TEXT, text.value).also(this::append).also { it.bind(text) }.also(init)


@KWebViewDsl
fun ViewCollection<in MultilineInputView>.multilineInputView(type: InputType = InputType.TEXT, text: String = "", init: MultilineInputView.() -> Unit = {}) =
        MultilineInputView(type, text).also(this::append).also(init)

@KWebViewDsl
fun ViewCollection<in MultilineInputView>.multilineInputView(type: InputType = InputType.TEXT, text: ReadOnlyProperty<String>, init: MultilineInputView.() -> Unit = {}) =
        MultilineInputView(type, text.value).also(this::append).also { it.bind(text) }.also(init)

@KWebViewDsl
fun ViewCollection<in MultilineInputView>.multilineInputView(type: InputType = InputType.TEXT, text: Property<String>, init: MultilineInputView.() -> Unit = {}) =
        MultilineInputView(type, text.value).also(this::append).also { it.bind(text) }.also(init)

@KWebViewDsl
fun ViewCollection<in MultilineInputView>.multilineInputView(type: InputType = InputType.TEXT, text: ValidationProperty<String>, init: MultilineInputView.() -> Unit = {}) =
        MultilineInputView(type, text.value).also(this::append).also { it.bind(text) }.also(init)
