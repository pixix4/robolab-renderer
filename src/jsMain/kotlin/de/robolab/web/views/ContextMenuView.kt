package de.robolab.web.views

import de.robolab.app.model.file.toFixed
import de.robolab.renderer.data.Point
import de.robolab.utils.*
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.list.ObservableReadOnlyList
import de.westermann.kobserve.list.asObservable
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import de.westermann.kwebview.View
import de.westermann.kwebview.components.*
import de.westermann.kwebview.extra.listFactory
import kotlin.browser.document

class ContextMenuView(
        menu: ContextMenu
) : View() {

    val onClose = EventHandler<Unit>()

    private val selectedProperty = property(menu.entry)

    private val nameProperty = selectedProperty.mapBinding { it.label }
    private val parentProperty = selectedProperty.mapBinding { it.parent }
    private val canGoBackProperty = parentProperty.mapBinding { it != null }

    private val contentListProperty: ReadOnlyProperty<ObservableReadOnlyList<ContextMenuEntry>> = selectedProperty.mapBinding {
        it.entries.toMutableList().asObservable()
    }

    init {
        val container = BoxView()
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
    }


    companion object {

        fun open(menu: ContextMenu) {
            val view = ContextMenuView(menu)
            Body.append(view)

            view.onClose {
                Body.remove(view)
            }
        }

        fun open(position: Point, name: String, init: MenuBuilder.() -> Unit) {
            open(menu(position, name, init))
        }
    }
}
