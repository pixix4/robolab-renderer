package de.robolab.client.web.views

import de.robolab.client.app.controller.FileImportController
import de.robolab.client.app.controller.NavigationBarController
import de.robolab.client.app.model.INavigationBarEntry
import de.robolab.client.app.model.INavigationBarPlottable
import de.robolab.client.web.openFile
import de.robolab.client.web.readText
import de.robolab.client.web.views.utils.buttonGroup
import de.robolab.common.utils.Logger
import de.robolab.common.utils.Point
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.not
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.*
import de.westermann.kwebview.extra.listFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NavigationBar(
    private val navigationBarController: NavigationBarController,
    private val fileImportController: FileImportController,
    navigationBarActiveProperty: ObservableProperty<Boolean>
) :
    ViewCollection<View>() {

    private fun createEntry(entry: INavigationBarEntry) = NavigationBarEntry(entry, navigationBarController)

    init {
        classList.bind("active", navigationBarActiveProperty)

        boxView("navigation-bar-header") {
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
        boxView("navigation-bar-content") {
            boxView("navigation-bar-search", "button-group") {
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

                button {
                    iconView(MaterialIcon.ADD)

                    onClick {
                        GlobalScope.launch(Dispatchers.Main) {
                            val files =
                                openFile(*fileImportController.supportedFileTypes.toTypedArray())

                            for (file in files) {
                                val content = file.readText()
                                if (content != null) {
                                    fileImportController.importFile(file.name, content)
                                }
                            }
                        }
                    }
                }
            }
            boxView("navigation-bar-group-head") {
                textView(navigationBarController.selectedGroupProperty.nullableFlatMapBinding { it?.tabNameProperty }
                    .mapBinding {
                        it ?: ""
                    })

                classList.bind("active", navigationBarController.selectedGroupProperty.mapBinding { it != null })

                onClick {
                    navigationBarController.closeGroup()
                }
            }
            boxView("navigation-bar-list") {
                listFactory(navigationBarController.filteredEntryListProperty, this@NavigationBar::createEntry)
            }
            boxView("navigation-bar-empty") {
                textView("Nothing to show!")
            }
        }
        boxView("navigation-bar-footer") {
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

        // Close navigation bar on mobile
        onClick {
            if (it.target == html && navigationBarActiveProperty.value) {
                navigationBarActiveProperty.value = false
                it.preventDefault()
                it.stopPropagation()
            }
        }
    }
}

class NavigationBarEntry(entry: INavigationBarEntry, navigationBarController: NavigationBarController) :
    ViewCollection<View>() {

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
