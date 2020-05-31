package de.robolab.client.web.views

import de.robolab.client.app.controller.SideBarController
import de.robolab.client.app.model.ISideBarEntry
import de.robolab.client.app.model.ISideBarPlottable
import de.robolab.client.web.views.utils.buttonGroup
import de.robolab.common.utils.Point
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.not
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.*
import de.westermann.kwebview.extra.listFactory

class SideBar(private val sideBarController: SideBarController, sideBarActiveProperty: ObservableProperty<Boolean>) :
    ViewCollection<View>() {

    private fun createEntry(entry: ISideBarEntry) = SideBarEntry(entry, sideBarController)

    init {
        classList.bind("active", sideBarActiveProperty)

        boxView("side-bar-header") {
            buttonGroup {
                for (tab in SideBarController.Tab.values()) {
                    button(tab.label) {
                        classList.bind("active", sideBarController.tabProperty.mapBinding { it == tab })
                        onClick {
                            sideBarController.tabProperty.value = tab
                        }
                    }
                }
            }
        }
        boxView("side-bar-content") {
            boxView("side-bar-search") {
                val searchView = inputView(InputType.SEARCH, sideBarController.searchStringProperty) {
                    placeholder = "Search…"
                }

                iconView(MaterialIcon.CANCEL) {
                    onClick {
                        it.preventDefault()
                        it.stopPropagation()
                        sideBarController.searchStringProperty.value = ""
                        searchView.focus()
                    }
                }
            }
            boxView("side-bar-group-head") {
                textView(sideBarController.selectedGroupProperty.nullableFlatMapBinding { it?.tabNameProperty }
                    .mapBinding {
                        it ?: ""
                    })

                classList.bind("active", sideBarController.selectedGroupProperty.mapBinding { it != null })

                onClick {
                    sideBarController.closeGroup()
                }
            }
            boxView("side-bar-list") {
                listFactory(sideBarController.filteredEntryListProperty, this@SideBar::createEntry)
            }
            boxView("side-bar-empty") {
                textView("Nothing to show!")
            }
        }
        boxView("side-bar-footer") {
            classList.bind(
                "success",
                sideBarController.statusColor.mapBinding { it == SideBarController.StatusColor.SUCCESS })
            classList.bind(
                "warn",
                sideBarController.statusColor.mapBinding { it == SideBarController.StatusColor.WARN })
            classList.bind(
                "error",
                sideBarController.statusColor.mapBinding { it == SideBarController.StatusColor.ERROR })

            textView(sideBarController.statusMessage)
            textView(sideBarController.statusActionLabel) {
                onClick {
                    sideBarController.onStatusAction()
                }
            }
        }

        // Close side bar on mobile
        onClick {
            if (it.target == html && sideBarActiveProperty.value) {
                sideBarActiveProperty.value = false
                it.preventDefault()
                it.stopPropagation()
            }
        }
    }
}

class SideBarEntry(entry: ISideBarEntry, sideBarController: SideBarController) : ViewCollection<View>() {

    private val selectedProperty = sideBarController.selectedElementListProperty.mapBinding { entry in it }

    init {
        textView(entry.titleProperty)
        textView(entry.subtitleProperty)
        iconView(MaterialIcon.SAVE) {
            title = "Unsaved changes"
            classList.bind("active", entry.unsavedChangesProperty)
        }

        classList.bind("active", selectedProperty)

        if (entry is ISideBarPlottable) {
            classList.bind("disabled", !entry.enabledProperty)
        }

        onClick {
            sideBarController.open(entry)
        }

        onContext { event ->
            event.stopPropagation()
            event.preventDefault()
            if (entry.hasContextMenu) {
                val menu = entry.buildContextMenu(Point(event.clientX, event.clientY))
                ContextMenuView.open(menu)
            }
        }
    }
}
