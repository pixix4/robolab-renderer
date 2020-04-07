package de.robolab.jfx.view

import de.robolab.app.controller.SideBarController
import de.robolab.jfx.adapter.toFx
import de.westermann.kobserve.property.FunctionAccessor
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.robolab.jfx.utils.icon
import javafx.scene.layout.Priority
import javafx.scene.text.FontWeight
import tornadofx.*
import tornadofx.FX.Companion.icon

class SideBar(sideBarController: SideBarController) : View() {

    override val root = vbox {
        toolbar {
            hbox {
                for (tab in SideBarController.Tab.values()) {
                    val buttonProperty = property(object : FunctionAccessor<Boolean> {
                        override fun set(value: Boolean): Boolean {
                            if (value) {
                                sideBarController.tabProperty.value = tab
                            }
                            return true
                        }

                        override fun get(): Boolean {
                            return  sideBarController.tabProperty.value == tab
                        }

                    }, sideBarController.tabProperty)

                    togglebutton(tab.label) {
                        selectedProperty().bindBidirectional(buttonProperty.toFx())
                        selectedProperty().onChange {
                            isSelected = buttonProperty.value
                        }
                    }
                }
            }
        }
        listview(sideBarController.entryListProperty.mapBinding { it.toFx() }.toFx()) {
            vgrow = Priority.ALWAYS
            cellFormat { provider ->
                graphic = vbox {
                    hbox {
                        vbox {
                            label(provider.nameProperty.toFx()) {
                                style {
                                    fontWeight = FontWeight.BOLD
                                }
                            }
                            label(provider.statusProperty.toFx())
                        }
                        spacer()
                        vbox {
                            spacer()
                            icon(MaterialIcon.SAVE) {
                                tooltip("Unsaved changes")
                                visibleWhen(provider.unsavedChangesProperty.toFx())
                            }
                            spacer()
                        }
                    }

                }
            }

            onUserSelect(1) {
                sideBarController.selectedEntryProperty.value = it
            }
        }

        hbox { }
    }
}
