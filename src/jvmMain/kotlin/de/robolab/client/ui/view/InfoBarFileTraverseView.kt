package de.robolab.client.ui.view

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.controller.TraverserBarController
import de.robolab.client.app.model.traverser.ITraverserStateEntry
import de.robolab.client.ui.adapter.toFx
import de.robolab.client.ui.style.MainStyle
import de.robolab.client.ui.utils.buttonGroup
import de.robolab.client.ui.view.iconNoAdd
import de.westermann.kobserve.event.EventListener
import de.westermann.kobserve.not
import de.westermann.kobserve.or
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.flatMapMutableBinding
import de.westermann.kobserve.property.mapBinding
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import tornadofx.*

class InfoBarFileTraverseView(private val traverserProperty: de.westermann.kobserve.base.ObservableValue<TraverserBarController>) : View() {


    private lateinit var characteristicListView: HBox
    private val characteristicList = traverserProperty.mapBinding { it.characteristicList }
    private var reference: EventListener<*>? = null

    override val root = vbox {
        vgrow = Priority.ALWAYS
        hgrow = Priority.ALWAYS

        toolbar {
            label(traverserProperty.flatMapBinding { it.traverserTitle }.toFx())

            spacer()

            button {
                graphic = iconNoAdd(MaterialIcon.REFRESH)

                setOnAction {
                    // traverserProperty.value.rerun()
                }
            }
        }

        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        listview(traverserProperty.mapBinding { it.entryList.toFx() }.toFx() as ObservableValue<ObservableList<ITraverserStateEntry>>) {
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS

            cellFormat { provider ->
                graphic = vbox {
                    addClass(MainStyle.listCellGraphic)
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

                    label(provider.visibleDetails.mapBinding { details -> details.joinToString("\n") { "- $it" } }
                        .toFx()) {
                        visibleWhen(provider.selected.toFx())
                    }
                }
            }

            addEventFilter(MouseEvent.MOUSE_CLICKED) { event ->
                val selectedItem = this.selectedItem
                if (selectedItem != null && event.target.isInsideRow()) {
                    selectedItem.select()
                }
            }

            addEventFilter(KeyEvent.KEY_PRESSED) { event ->
                val selectedItem = this.selectedItem
                if (selectedItem != null) {
                    when (event.code) {
                        KeyCode.LEFT -> {
                            if (selectedItem.isPreviousEnabled.value) {
                                val index = this.selectionModel.selectedIndices?.firstOrNull()

                                selectedItem.clickPreviousOption()

                                if (index != null) {
                                    this.selectionModel.select(index)
                                    this.selectedItem?.select()
                                }
                            }
                        }
                        KeyCode.RIGHT -> {
                            if (selectedItem.isNextEnabled.value) {
                                val index = this.selectionModel.selectedIndices?.firstOrNull()

                                selectedItem.clickNextOption()

                                if (index != null) {
                                    this.selectionModel.select(index)
                                    this.selectedItem?.select()
                                }
                            }
                        }
                        else -> return@addEventFilter
                    }
                }
                event.consume()
                this.requestFocus()
            }

            addEventFilter(KeyEvent.KEY_RELEASED) { event ->
                val selectedItem = this.selectedItem
                if (selectedItem != null) {
                    when (event.code) {
                        KeyCode.ENTER -> selectedItem.select()
                        KeyCode.SPACE -> selectedItem.select()
                        else -> {
                            if (!selectedItem.selected.value) {
                                selectedItem.select()
                            }
                        }
                    }
                }
                event.consume()
                this.requestFocus()
            }
        }

        vbox {
            toolbar {
                button {
                    enableWhen(traverserProperty.flatMapBinding { it.isPreviousEnabled }.toFx())

                    graphic = iconNoAdd(MaterialIcon.CHEVRON_LEFT)

                    setOnAction {
                        traverserProperty.value.clickPreviousTrail()
                    }
                }

                spacer()
                label(traverserProperty.flatMapBinding { it.traverserTitle }.toFx())
                spacer()

                buttonGroup {
                    button {
                        graphic = iconNoAdd(MaterialIcon.ARROW_DROP_DOWN)
                        title = "Expand"

                        enableWhen { traverserProperty.flatMapBinding { it.autoExpandProperty }.not().toFx() }

                        setOnAction {
                            traverserProperty.value.clickFullExpand()
                        }
                    }
                    button {
                        graphic = iconNoAdd(MaterialIcon.ARROW_DOWNWARD)

                        bindSelectedProperty(traverserProperty.flatMapMutableBinding { it.autoExpandProperty })
                    }
                }

                button {
                    enableWhen(traverserProperty.flatMapBinding { it.isNextEnabled }.toFx())

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
