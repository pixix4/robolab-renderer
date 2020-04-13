package de.robolab.jfx.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.robolab.app.controller.SideBarController
import de.robolab.jfx.adapter.toFx
import de.robolab.jfx.style.MainStyle
import de.robolab.jfx.utils.buttonGroup
import de.robolab.jfx.utils.icon
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.property.FunctionAccessor
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import javafx.css.Styleable
import javafx.scene.layout.Priority
import javafx.scene.text.FontWeight
import tornadofx.*

class SideBar(sideBarController: SideBarController) : View() {

    override val root = vbox {
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
        listview(sideBarController.entryListProperty.mapBinding { it.toFx() }.toFx()) {
            vgrow = Priority.ALWAYS
            cellFormat { provider ->
                graphic = vbox {
                    hbox {
                        vbox {
                            label(provider.titleProperty.toFx()) {
                                style {
                                    fontWeight = FontWeight.BOLD
                                }
                            }
                            label(provider.subtitleProperty.toFx())
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

            label(sideBarController.statusMessage.toFx())
            spacer()
            label(sideBarController.statusActionLabel.toFx()) {
                setOnMouseClicked {
                    sideBarController.onStatusAction()
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
