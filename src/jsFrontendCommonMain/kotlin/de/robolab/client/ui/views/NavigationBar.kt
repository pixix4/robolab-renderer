package de.robolab.client.ui.views

import de.robolab.client.app.controller.FileImportController
import de.robolab.client.app.controller.NavigationBarController
import de.robolab.client.app.controller.UiController
import de.robolab.client.app.model.base.INavigationBarEntry
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.ui.lineSequence
import de.robolab.client.ui.openFile
import de.robolab.client.ui.pathOrName
import de.robolab.client.ui.readText
import de.robolab.client.utils.electron
import de.robolab.client.utils.noElectron
import de.robolab.common.utils.Point
import de.westermann.kobserve.event.now
import de.westermann.kobserve.not
import de.westermann.kobserve.property.mapBinding
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
    private val uiController: UiController
) : ViewCollection<View>() {

    private fun createEntry(entry: INavigationBarEntry) = NavigationBarEntry(entry)

    private fun BoxView.setupTabs() {
        clear()

        for ((index, tab) in navigationBarController.tabListProperty.value.withIndex()) {
            boxView("tab-bar-item") {
                classList.bind("active", navigationBarController.tabProperty.mapBinding { it == tab })

                iconView(tab.iconProperty)
                tab.nameProperty.onChange.now {
                    title = tab.nameProperty.value
                }

                onClick {
                    navigationBarController.tabIndexProperty.value = index
                }
            }
        }
    }

    init {
        boxView("navigation-bar-header", "tab-bar") {
            navigationBarController.tabListProperty.onChange.now {
                setupTabs()
            }
        }
        boxView("navigation-bar-content") {
            boxView("navigation-bar-group-head") {
                val labelText = navigationBarController.backButtonLabelProperty

                iconView(MaterialIcon.ARROW_BACK)
                textView(labelText)
                title = labelText.value
                labelText.onChange {
                    title = labelText.value
                }

                classList.bind("active", navigationBarController.backButtonEnabledProperty)

                onClick {
                    navigationBarController.onBackButtonClick()
                }
            }
            boxView("navigation-bar-list") {
                listFactory(navigationBarController.entryListProperty, this@NavigationBar::createEntry)
            }
            boxView("navigation-bar-empty") {
                textView("Nothing to show!")
            }
        }

        boxView("navigation-bar-search", "button-group", "button-form-group") {
            val searchView = inputView(InputType.SEARCH, navigationBarController.searchStringProperty) {
                placeholder = "Search…"

                onKeyDown {
                    if (it.keyCode == 13) {
                        navigationBarController.submitSearch()
                    }
                }
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
                iconView(MaterialIcon.PUBLISH)

                onClick {
                    GlobalScope.launch(Dispatchers.Main) {
                        val files =
                            openFile(*fileImportController.supportedFileTypes.toTypedArray())

                        for (file in files) {
                            val content = file.readText()
                            if (content != null) {
                                fileImportController.importFile(file.pathOrName()) {
                                    file.lineSequence()
                                }
                            }
                        }
                    }
                }
            }
        }

        // Close navigation bar on mobile
        onClick {
            if (it.target == html && uiController.navigationBarEnabledProperty.value) {
                uiController.navigationBarEnabledProperty.value = false
                it.preventDefault()
                it.stopPropagation()
            }
        }
    }
}

class NavigationBarEntry(entry: INavigationBarEntry) :
    ViewCollection<View>() {

    init {
        textView(entry.nameProperty)
        textView(entry.subtitleProperty)
        boxView {
            entry.statusIconProperty.onChange.now {
                clear()
                for (icon in entry.statusIconProperty.value) {
                    iconView(icon)
                }
            }
        }

        classList.bind("disabled", !entry.enabledProperty)

        onClick {
            entry.open(it.ctrlKey)
        }
        onAuxClick { event ->
            val which = event.asDynamic().which as Int
            if (which == 2) {
                event.preventDefault()
                entry.open(true)
            }
        }

        onContext { event ->
            event.stopPropagation()
            event.preventDefault()

            val menu = entry.generateContextMenuAt(Point(event.clientX, event.clientY))
            if (menu != null) {
                electron { electron ->
                    electron.menu(menu)
                }
                noElectron {
                    ContextMenuView.open(menu)
                }
            }
        }
    }
}