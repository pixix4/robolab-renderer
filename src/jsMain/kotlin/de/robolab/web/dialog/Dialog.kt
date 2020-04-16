package de.robolab.web.dialog

import de.robolab.utils.PreferenceStorage
import de.westermann.kobserve.Property
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.FunctionAccessor
import de.westermann.kobserve.property.property
import de.westermann.kwebview.View
import de.westermann.kwebview.components.*

abstract class Dialog(title: String) {

    val onClose = EventHandler<Unit>()

    private val titleProperty = property(title)

    protected abstract fun BoxView.buildContent()


    protected fun BoxView.dialogFormGroup(name: String, block: BoxView.() -> Unit) {
        boxView("dialog-form-group") {
            textView(name) {
                classList += "dialog-form-group-header"
            }

            block()
        }
    }

    protected fun BoxView.dialogFormEntry(name: String, block: BoxView.() -> Unit) {
        boxView("dialog-form-entry") {
            textView(name)

            block()
        }
    }

    private fun build(): View {
        val dialogContainer = BoxView()
        dialogContainer.classList += "dialog"

        dialogContainer.boxView("dialog-window") {

            boxView("dialog-header") {
                textView(titleProperty)

                button {
                    iconView(MaterialIcon.CLOSE)
                    onClick {
                        onClose.emit(Unit)
                    }
                }
            }

            boxView("dialog-body") {
                buildContent()
            }

            onClick { event ->
                event.stopPropagation()
            }
        }

        dialogContainer.onClick { event ->
            event.stopPropagation()
            onClose.emit(Unit)
        }

        return dialogContainer
    }

    companion object {

        fun open(dialog: Dialog) {
            val dialogView = dialog.build()
            Body.append(dialogView)

            dialog.onClose {
                Body.remove(dialogView)
            }
        }
    }
}

fun Property<Double>.bindStringParsing() = property(object : FunctionAccessor<String> {
    override fun set(value: String): Boolean {
        this@bindStringParsing.value = value.toDoubleOrNull() ?: return false
        return true
    }

    override fun get(): String {
        return this@bindStringParsing.value.toString()
    }

}, this)
