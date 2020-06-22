package de.robolab.client.ui.dialog

import de.robolab.common.utils.Point
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.EventListener
import de.westermann.kobserve.property.DelegatePropertyAccessor
import de.westermann.kobserve.property.property
import de.westermann.kwebview.View
import de.westermann.kwebview.clientPosition
import de.westermann.kwebview.components.*
import de.westermann.kwebview.extra.tabView
import org.w3c.dom.get

abstract class Dialog(title: String) {

    val onClose = EventHandler<Unit>()

    private val titleProperty = property(title)

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

            boxView("dialog-form-flex") {
                block()
            }
        }
    }

    private val tabList = mutableListOf<Pair<String, BoxView.() -> Unit>>()
    protected fun tab(name: String = "", init: BoxView.() -> Unit) {
        tabList += name to init
    }

    private fun BoxView.initSingleTab() {
        val (_, init) = tabList.single()

        init()
    }

    private fun BoxView.initMultiTab() {
        tabView {
            for ((name, init) in tabList) {
                tab(name, init)
            }
        }
    }

    private fun build(): View {
        val dialogContainer = BoxView()
        dialogContainer.classList += "dialog"

        dialogContainer.boxView("dialog-window") window@{

            boxView("dialog-header") {
                textView(titleProperty)

                boxView {
                    button {
                        iconView(MaterialIcon.CLOSE)

                        onClick {
                            onClose.emit(Unit)
                        }
                    }
                    onMouseDown { it.stopPropagation() }
                    onTouchStart { it.stopPropagation() }
                }

                var isDragged = false
                var lastPoint = Point.ZERO
                var offset = Point.ZERO
                onMouseDown { event ->
                    isDragged = true
                    lastPoint = event.clientPosition
                    event.stopPropagation()
                    event.preventDefault()
                }
                onMouseMove { event ->
                    if (isDragged) {
                        val point = event.clientPosition

                        offset += point - lastPoint
                        this@window.style {
                            left = "${offset.left}px"
                            top = "${offset.top}px"
                        }

                        lastPoint = point
                        event.stopPropagation()
                        event.preventDefault()
                    }
                }
                onMouseUp {
                    isDragged = false
                }
                onMouseLeave {
                    isDragged = false
                }
                onTouchStart { event ->
                    lastPoint = event.touches[0]?.let { Point(it.clientX, it.clientY) } ?: Point.ZERO
                    event.stopPropagation()
                    event.preventDefault()
                }
                onTouchMove { event ->
                    val point = event.touches[0]?.let { Point(it.clientX, it.clientY) } ?: Point.ZERO

                    offset += point - lastPoint
                    this@window.style {
                        left = "${offset.left}px"
                        top = "${offset.top}px"
                    }

                    lastPoint = point
                    event.stopPropagation()
                    event.preventDefault()
                }
            }

            boxView("dialog-body") {
                when {
                    tabList.size <= 0 -> {
                        // Nothing to do
                    }
                    tabList.size == 0 -> {
                        initSingleTab()
                    }
                    else -> {
                        initMultiTab()
                    }
                }
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

            var reference: EventListener<*>? = null

            fun close() {
                Body.remove(dialogView)
                reference?.detach()
            }

            reference = Body.onKeyDown.reference {
                if (it.keyCode == 27) {
                    close()
                }
            }

            dialog.onClose {
                close()
            }
        }
    }
}

fun ObservableProperty<Double>.bindStringParsing() = property(object : DelegatePropertyAccessor<String> {
    override fun set(value: String) {
        this@bindStringParsing.value = value.toDoubleOrNull() ?: return
    }

    override fun get(): String {
        return this@bindStringParsing.value.toString()
    }

}, this)

fun ObservableProperty<Int>.bindStringParsing() = property(object : DelegatePropertyAccessor<String> {
    override fun set(value: String) {
        this@bindStringParsing.value = value.toIntOrNull() ?: return
    }

    override fun get(): String {
        return this@bindStringParsing.value.toString()
    }

}, this)
