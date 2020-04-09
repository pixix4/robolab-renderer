package de.robolab.web.views

import de.robolab.app.controller.SideBarController
import de.robolab.app.model.ISideBarEntry
import de.robolab.app.model.ISideBarPlottable
import de.robolab.web.views.utils.buttonGroup
import de.westermann.kobserve.Property
import de.westermann.kobserve.not
import de.westermann.kobserve.property.flatMapReadOnlyNullableBinding
import de.westermann.kobserve.property.mapBinding
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.*
import de.westermann.kwebview.extra.listFactory

class SideBar(private val sideBarController: SideBarController, sideBarProperty: Property<Boolean>) : ViewCollection<View>() {

    private fun createEntry(entry: ISideBarEntry) = SideBarEntry(entry, sideBarController)

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
            boxView("side-bar-group-head") {
                textView(sideBarController.selectedGroupProperty.flatMapReadOnlyNullableBinding { it?.tabNameProperty }.mapBinding {
                    it ?: ""
                })

                classList.bind("active", sideBarController.selectedGroupProperty.mapBinding { it != null })

                onClick {
                    sideBarController.closeGroup()
                }
            }
            boxView("side-bar-list") {
                listFactory(sideBarController.entryListProperty, this@SideBar::createEntry)
            }
            boxView("side-bar-empty") {
                textView("Nothing to show!")
            }
        }
        boxView("side-bar-footer") {
            classList.bind("success", sideBarController.statusColor.mapBinding { it == SideBarController.StatusColor.SUCCESS })
            classList.bind("warn", sideBarController.statusColor.mapBinding { it == SideBarController.StatusColor.WARN })
            classList.bind("error", sideBarController.statusColor.mapBinding { it == SideBarController.StatusColor.ERROR })

            textView(sideBarController.statusMessage)
            textView(sideBarController.statusActionLabel) {
                onClick {
                    sideBarController.onStatusAction()
                }
            }
        }

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
    }
}
