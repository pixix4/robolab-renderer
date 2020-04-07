package de.robolab.web.views

import de.robolab.app.controller.SideBarController
import de.robolab.app.model.IPlottable
import de.robolab.web.views.utils.buttonGroup
import de.westermann.kobserve.Property
import de.westermann.kobserve.property.mapBinding
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.*
import de.westermann.kwebview.extra.listFactory

class SideBar(private val sideBarController: SideBarController, sideBarProperty: Property<Boolean>) : ViewCollection<View>() {

    private fun createEntry(entry: IPlottable) = SideBarEntry(entry, sideBarController)

    init {
        classList.bind("active", sideBarProperty)

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
            listFactory(sideBarController.entryListProperty, this@SideBar::createEntry)
        }
        boxView("side-bar-empty") {
            textView("Nothing to show!")
        }
        boxView("side-bar-footer")

        // Close side bar on mobile
        onClick {
            if (it.target == html && sideBarProperty.value) {
                sideBarProperty.value = false
                it.preventDefault()
                it.stopPropagation()
            }
        }
    }
}

class SideBarEntry(entry: IPlottable, sideBarController: SideBarController) : ViewCollection<View>() {

    private val selectedProperty = sideBarController.selectedEntryProperty.mapBinding { it == entry }

    init {
        textView(entry.nameProperty)
        textView(entry.statusProperty)
        iconView(MaterialIcon.SAVE) {
            title = "Unsaved changes"
            classList.bind("active", entry.unsavedChangesProperty)
        }

        classList.bind("active", selectedProperty)

        onClick {
            sideBarController.selectedEntryProperty.value = entry
        }
    }
}
