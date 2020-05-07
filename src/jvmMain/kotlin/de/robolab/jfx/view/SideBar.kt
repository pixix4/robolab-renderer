package de.robolab.jfx.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.robolab.app.controller.SideBarController
import de.robolab.app.model.ISideBarPlottable
import de.robolab.jfx.adapter.toFx
import de.robolab.jfx.style.MainStyle
import de.robolab.jfx.utils.buttonGroup
import de.robolab.jfx.utils.icon
import de.robolab.renderer.data.Point
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.not
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
import javafx.css.Styleable
import javafx.scene.control.OverrunStyle
import javafx.scene.layout.Priority
import javafx.scene.text.FontWeight
import tornadofx.*

class SideBar(sideBarController: SideBarController) : View() {

    override val root = vbox {
        addClass(MainStyle.sideBar)

        hbox {
            addClass(MainStyle.toolBar)
            hgrow = Priority.ALWAYS


            //spacer()
            buttonGroup {
                hgrow = Priority.ALWAYS
                for (tab in SideBarController.Tab.values()) {
                    button(tab.label) {
                        bindSelectedProperty(sideBarController.tabProperty.mapBinding { it == tab }) {
                            sideBarController.tabProperty.value = tab
                        }
                        textOverrun= OverrunStyle.CLIP

                        hgrow = Priority.ALWAYS
                    }
                }
            }
            //spacer()
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

        hbox {
            hgrow = Priority.ALWAYS
            val sideBarBackButton = hbox {
                addClass(MainStyle.sideBarBackButton)
                hgrow = Priority.ALWAYS

                label(sideBarController.selectedGroupProperty.nullableFlatMapBinding { it?.tabNameProperty }.mapBinding {
                    it ?: ""
                }.toFx()) {
                    style {
                        fontWeight = FontWeight.BOLD
                        padding = box(0.5.em, 1.em)
                    }
                }


                setOnMouseClicked {
                    sideBarController.closeGroup()
                }
            }

            val isVisible = sideBarController.selectedGroupProperty.mapBinding { it != null }

            if (!isVisible.value) {
                sideBarBackButton.removeFromParent()
            }

            isVisible.onChange {
                if (isVisible.value) {
                    add(sideBarBackButton)
                } else {
                    sideBarBackButton.removeFromParent()
                }
            }
        }

        listview(sideBarController.filteredEntryListProperty.mapBinding { it.toFx() }.toFx()) {
            vgrow = Priority.ALWAYS
            cellFormat { provider ->
                val selectedProperty = sideBarController.selectedElementListProperty.mapBinding { provider in it }

                bindClass(MainStyle.active, selectedProperty)
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

                    setOnContextMenuRequested {
                        if (provider.hasContextMenu) {
                            val menu = provider.buildContextMenu(Point.ZERO)

                            contextMenu = menu.toFx()
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

fun Styleable.bindClass(clazz: CssRule, property: ObservableValue<Boolean>) {
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
