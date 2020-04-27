package de.robolab.jfx.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.robolab.app.controller.TraverserBarController
import de.robolab.app.model.traverser.ITraverserStateEntry
import de.robolab.jfx.adapter.toFx
import de.robolab.jfx.style.MainStyle
import de.robolab.jfx.utils.buttonGroup
import de.robolab.jfx.utils.iconNoAdd
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.event.EventListener
import de.westermann.kobserve.not
import de.westermann.kobserve.or
import de.westermann.kobserve.property.flatMapReadOnlyBinding
import de.westermann.kobserve.property.mapBinding
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import tornadofx.*

class InfoBarTraverserView(private val traverserProperty: ReadOnlyProperty<TraverserBarController>) : View() {


    private lateinit var characteristicListView: HBox
    private val characteristicList = traverserProperty.mapBinding { it.characteristicList }
    private var reference: EventListener<*>? = null

    override val root = vbox {
        vgrow = Priority.ALWAYS
        hgrow = Priority.ALWAYS

        toolbar {
            label(traverserProperty.flatMapReadOnlyBinding { it.traverserTitle }.toFx())

            spacer()

            button {
                graphic = iconNoAdd(MaterialIcon.REFRESH)

                setOnAction {
                    // traverserProperty.value.rerun()
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        listview(traverserProperty.mapBinding { it.entryList.toFx() }.toFx() as ObservableValue<ObservableList<ITraverserStateEntry>>) {
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS

            cellFormat { provider ->
                graphic = vbox {

                    bindClass(MainStyle.active, provider.selected)

                    hbox {
                        val buttonsVisible = (provider.isPreviousEnabled or provider.isNextEnabled).toFx()
                        button {
                            enableWhen(provider.isPreviousEnabled.toFx())
                            visibleWhen(buttonsVisible)

                            graphic = iconNoAdd(MaterialIcon.CHEVRON_LEFT)

                            setOnAction {
                                provider.clickPreviousOption()
                            }
                        }

                        spacer()
                        label(provider.visibleTitle.toFx())
                        spacer()

                        button {
                            enableWhen(provider.isNextEnabled.toFx())
                            visibleWhen(buttonsVisible)

                            graphic = iconNoAdd(MaterialIcon.CHEVRON_RIGHT)

                            setOnAction {
                                provider.clickNextOption()
                            }
                        }
                    }

                    label(provider.visibleDetails.mapBinding { details -> details.joinToString("\n") { "- $it" } }.toFx()) {
                        visibleWhen(provider.selected.toFx())
                    }
                }
            }

            onUserSelect(1) {
                it.select()
            }
        }

        vbox {
            toolbar {
                button {
                    enableWhen(traverserProperty.flatMapReadOnlyBinding { it.isPreviousEnabled }.toFx())

                    graphic = iconNoAdd(MaterialIcon.CHEVRON_LEFT)

                    setOnAction {
                        traverserProperty.value.clickPreviousTrail()
                    }
                }

                spacer()
                label(traverserProperty.flatMapReadOnlyBinding { it.traverserTitle }.toFx())
                spacer()

                buttonGroup {
                    button {
                        graphic = iconNoAdd(MaterialIcon.ARROW_DROP_DOWN)
                        title = "Expand"

                        enableWhen { traverserProperty.flatMapReadOnlyBinding { it.autoExpandProperty }.not().toFx() }

                        setOnAction {
                                traverserProperty.value.clickFullExpand()
                        }
                    }
                    togglebutton {
                        graphic = iconNoAdd(MaterialIcon.ARROW_DOWNWARD)

                        bindSelectedProperty(traverserProperty.flatMapReadOnlyBinding { it.autoExpandProperty }) {
                            traverserProperty.value.autoExpandProperty.value = !traverserProperty.value.autoExpandProperty.value
                        }
                    }
                }

                button {
                    enableWhen(traverserProperty.flatMapReadOnlyBinding { it.isNextEnabled }.toFx())

                    graphic = iconNoAdd(MaterialIcon.CHEVRON_RIGHT)

                    setOnAction {
                        traverserProperty.value.clickNextTrail()
                    }
                }
            }

            characteristicListView = hbox {
                style {
                    padding = box(0.5.em)
                }
            }

            updateCharacteristicList()
        }
    }

    private fun updateCharacteristicList() {
        reference?.detach()
        
        val list = characteristicList.value
        reference = list.onChange.reference {
            updateCharacteristicList()
        }

        characteristicListView.clear()
        
        for (entry in list) {
            characteristicListView.hbox {
                style {
                    prefWidth = 1.em
                    prefHeight = 1.em
                    backgroundColor = multi(entry.color.toFx())
                }
            }
        }
    }

    init {
        characteristicList.onChange {
            updateCharacteristicList()
        }
    }
}
