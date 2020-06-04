package de.robolab.client.web.views

import de.robolab.client.app.controller.NavigationBarController
import de.robolab.client.app.model.INavigationBarEntry
import de.robolab.client.app.model.INavigationBarPlottable
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

class SideBar(private val navigationBarController: NavigationBarController, sideBarActiveProperty: ObservableProperty<Boolean>) :
    ViewCollection<View>() {

    private fun createEntry(entry: INavigationBarEntry) = SideBarEntry(entry, navigationBarController)

    init {
        classList.bind("active", sideBarActiveProperty)

        boxView("side-bar-header") {
            buttonGroup {
                for (tab in NavigationBarController.Tab.values()) {
                    button(tab.label) {
                        classList.bind("active", navigationBarController.tabProperty.mapBinding { it == tab })
                        onClick {
                            navigationBarController.tabProperty.value = tab
                        }
                    }
                }
            }
        }
        boxView("side-bar-content") {
            boxView("side-bar-search") {
                val searchView = inputView(InputType.SEARCH, navigationBarController.searchStringProperty) {
                    placeholder = "Search…"
                }

                iconView(MaterialIcon.CANCEL) {
                    onClick {
                        it.preventDefault()
                        it.stopPropagation()
                        navigationBarController.searchStringProperty.value = ""
                        searchView.focus()
                    }
                }
            }
            boxView("side-bar-group-head") {
                textView(navigationBarController.selectedGroupProperty.nullableFlatMapBinding { it?.tabNameProperty }
                    .mapBinding {
                        it ?: ""
                    })

                classList.bind("active", navigationBarController.selectedGroupProperty.mapBinding { it != null })

                onClick {
                    navigationBarController.closeGroup()
                }
            }
            boxView("side-bar-list") {
                listFactory(navigationBarController.filteredEntryListProperty, this@SideBar::createEntry)
            }
            boxView("side-bar-empty") {
                textView("Nothing to show!")
            }
        }
        boxView("side-bar-footer") {
            classList.bind(
                "success",
                navigationBarController.statusColor.mapBinding { it == NavigationBarController.StatusColor.SUCCESS })
            classList.bind(
                "warn",
                navigationBarController.statusColor.mapBinding { it == NavigationBarController.StatusColor.WARN })
            classList.bind(
                "error",
                navigationBarController.statusColor.mapBinding { it == NavigationBarController.StatusColor.ERROR })

            textView(navigationBarController.statusMessage)
            textView(navigationBarController.statusActionLabel) {
                onClick {
                    navigationBarController.onStatusAction()
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

class SideBarEntry(entry: INavigationBarEntry, navigationBarController: NavigationBarController) : ViewCollection<View>() {

    private val selectedProperty = navigationBarController.selectedElementListProperty.mapBinding { entry in it }

    init {
        textView(entry.titleProperty)
        textView(entry.subtitleProperty)
        iconView(MaterialIcon.SAVE) {
            title = "Unsaved changes"
            classList.bind("active", entry.unsavedChangesProperty)
        }

        classList.bind("active", selectedProperty)

        if (entry is INavigationBarPlottable) {
            classList.bind("disabled", !entry.enabledProperty)
        }

        onClick {
            navigationBarController.open(entry)
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
