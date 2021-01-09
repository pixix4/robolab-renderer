package de.robolab.client.ui.views

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.utils.*
import de.robolab.common.parser.toFixed
import de.robolab.common.utils.Point
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.list.asObservable
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import de.westermann.kwebview.View
import de.westermann.kwebview.components.*
import de.westermann.kwebview.extra.listFactory
import kotlinx.browser.document
import kotlinx.browser.window
import kotlin.math.min

class ContextMenuView(
    private val menu: ContextMenu
) : View() {

    val onClose = EventHandler<Unit>()

    private val selectedProperty = property(menu.entry)

    private val nameProperty = selectedProperty.mapBinding { it.label }
    private val parentProperty = selectedProperty.mapBinding { it.parent }
    private val canGoBackProperty = parentProperty.mapBinding { it != null }

    private val contentListProperty: ObservableValue<ObservableList<ContextMenuEntry>> = selectedProperty.mapBinding {
        it.entries.toMutableList().asObservable()
    }

    private val container = BoxView()

    private fun fixMenuOutOfScreen() {
        val right = menu.position.left + container.clientWidth
        val bottom = menu.position.top + container.clientHeight

        val leftOffset = min(0.0, window.innerWidth - right)
        val topOffset = min(0.0, window.innerHeight - bottom)

        val left = menu.position.left + leftOffset
        val top = menu.position.top + topOffset

        document.body?.style?.setProperty("--context-menu-left", "${left.toFixed(2)}px")
        document.body?.style?.setProperty("--context-menu-top", "${top.toFixed(2)}px")
    }

    init {
        container.classList += "context-menu-window"

        document.body?.style?.setProperty("--context-menu-left", "${menu.position.left.toFixed(2)}px")
        document.body?.style?.setProperty("--context-menu-top", "${menu.position.top.toFixed(2)}px")

        container.boxView("context-menu-header") {
            iconView(MaterialIcon.NAVIGATE_BEFORE) {
                classList.bind("active", canGoBackProperty)
            }
            textView(nameProperty)

            onClick {
                val parent = selectedProperty.value.parent
                if (parent != null) {
                    selectedProperty.value = parent
                }
            }
        }

        container.boxView("context-menu-body") {
            listFactory(contentListProperty, factory = { entry ->
                val entryBox = BoxView()
                entryBox.classList += "context-menu-entry"
                entryBox.apply {
                    if (entry is ContextMenuAction && entry.checked != null) {
                        classList += "context-check-menu-entry"

                        if (entry.checked) {
                            iconView(MaterialIcon.CHECK)
                        }
                    }

                    textView(entry.label)
                    if (entry is ContextMenuList) {
                        iconView(MaterialIcon.NAVIGATE_NEXT)

                        onClick {
                            selectedProperty.value = entry
                        }
                    } else if (entry is ContextMenuAction) {
                        onClick {
                            entry.action()
                            onClose.emit(Unit)
                        }
                    }

                }
            })
        }

        container.onClick { event ->
            event.stopPropagation()
        }

        html.appendChild(container.html)

        onClick { event ->
            event.stopPropagation()
            onClose.emit(Unit)
        }

        onContext { event ->
            event.stopPropagation()
            event.preventDefault()
        }

        runAsync {
            fixMenuOutOfScreen()
        }
        selectedProperty.onChange {
            runAsync {
                fixMenuOutOfScreen()
            }
        }
    }


    companion object {

        fun open(menu: ContextMenu) {
            val view = ContextMenuView(menu)
            Body.append(view)

            view.onClose {
                Body.remove(view)
            }
        }
    }
}
