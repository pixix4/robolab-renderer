package de.robolab.jfx.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.robolab.app.controller.SideBarController
import de.robolab.app.model.ISideBarPlottable
import de.robolab.jfx.adapter.toFx
import de.robolab.jfx.style.MainStyle
import de.robolab.jfx.utils.buttonGroup
import de.robolab.jfx.utils.icon
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.not
import de.westermann.kobserve.property.FunctionAccessor
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import javafx.css.Styleable
import javafx.scene.layout.Priority
import javafx.scene.text.FontWeight
import tornadofx.*

class SideBar(sideBarController: SideBarController) : View() {

    override val root = vbox {
        addClass(MainStyle.sideBar)

        minWidth = 260.0

        toolbar {
            hgrow = Priority.ALWAYS

            spacer()
            buttonGroup {
                hgrow = Priority.ALWAYS
                for (tab in SideBarController.Tab.values()) {
                    val buttonProperty = property(object : FunctionAccessor<Boolean> {
                        override fun set(value: Boolean): Boolean {
                            if (value) {
                                sideBarController.tabProperty.value = tab
                            }
                            return true
                        }

                        override fun get(): Boolean {
                            return sideBarController.tabProperty.value == tab
                        }

                    }, sideBarController.tabProperty)

                    togglebutton(tab.label) {
                        selectedProperty().bindBidirectional(buttonProperty.toFx())
                        selectedProperty().onChange {
                            isSelected = buttonProperty.value
                        }

                        hgrow = Priority.ALWAYS
                    }
                }
            }
            spacer()
        }

        hbox {
            textfield(sideBarController.searchStringProperty.toFx()) {
                hgrow = Priority.ALWAYS
                promptText = "Search…"
            }

            style {
                padding = box(0.5.em)
            }
        }

        listview(sideBarController.filteredEntryListProperty.mapBinding { it.toFx() }.toFx()) {
            vgrow = Priority.ALWAYS
            cellFormat { provider ->
                graphic = vbox {
                    hbox {
                        style {
                            padding = box(0.4.em, 0.5.em)
                        }
                        if (provider is ISideBarPlottable) {
                            bindClass(MainStyle.disabled, !provider.enabledProperty)
                        }
                        vbox {
                            label(provider.titleProperty.toFx()) {
                                style {
                                    fontWeight = FontWeight.BOLD
                                }
                            }
                            label(provider.subtitleProperty.toFx()) {
                                style {
                                    fontSize = 0.8.em
                                }
                            }
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
                sideBarController.open(it)
            }
        }

        hbox {
            bindClass(MainStyle.success, sideBarController.statusColor.mapBinding { it == SideBarController.StatusColor.SUCCESS })
            bindClass(MainStyle.warn, sideBarController.statusColor.mapBinding { it == SideBarController.StatusColor.WARN })
            bindClass(MainStyle.error, sideBarController.statusColor.mapBinding { it == SideBarController.StatusColor.ERROR })

            style {
                prefHeight = 2.em
                padding = box(0.4.em, 0.5.em)
            }

            label(sideBarController.statusMessage.toFx())
            spacer()
            label(sideBarController.statusActionLabel.toFx()) {
                setOnMouseClicked {
                    sideBarController.onStatusAction()
                }

                style {
                    underline = true
                }
            }
        }
    }
}

fun Styleable.bindClass(clazz: CssRule, property: ReadOnlyProperty<Boolean>) {
    property.onChange {
        if (property.value) {
            addClass(clazz)
        } else {
            removeClass(clazz)
        }
    }

    if (property.value) {
        addClass(clazz)
    }
}
