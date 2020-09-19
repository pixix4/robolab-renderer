package de.robolab.client.ui.views.boxes

import de.robolab.client.app.controller.TraverserBarController
import de.robolab.client.app.controller.UiController
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.traverser.CharacteristicItem
import de.robolab.client.app.model.traverser.ITraverserStateEntry
import de.robolab.client.ui.views.utils.buttonGroup
import de.robolab.client.utils.runAsync
import de.westermann.kobserve.and
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.list.asObservable
import de.westermann.kobserve.not
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.mapBinding
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.*
import de.westermann.kwebview.extra.listFactory
import de.westermann.kwebview.extra.scrollBoxView

class InfoBarFileTraverseView(
    private val traverserProperty: ObservableValue<TraverserBarController>,
    private val uiController: UiController
) : ViewCollection<View>() {

    init {
        scrollBoxView {
            uiController.infoBarVisibleProperty.onChange {
                runAsync {
                    updateScrollBox()
                }
            }
            resizeBox(1.0) {
                boxView("traverser-bar-header") {
                    textView(traverserProperty.flatMapBinding { it.traverserTitle })

                    button {
                        iconView(MaterialIcon.REFRESH)

                        onClick {
                            // traverserProperty.value.rerun()
                        }
                    }
                }

                boxView("traverser-bar-body") {
                    listFactory(traverserProperty.mapBinding {
                        @Suppress("USELESS_CAST")
                        it.entryList as ObservableList<ITraverserStateEntry>
                    }, factory = { entry ->
                        TraverserEntryView(entry)
                    })
                }

                boxView("traverser-bar-footer") {
                    boxView("traverser-bar-trail") {
                        button {
                            disabledProperty.bind(!traverserProperty.flatMapBinding { it.isPreviousEnabled })
                            iconView(MaterialIcon.CHEVRON_LEFT)

                            onClick { event ->
                                traverserProperty.value.clickPreviousTrail()
                                event.stopPropagation()
                            }
                        }

                        textView(traverserProperty.flatMapBinding { it.traverserTitle })

                        buttonGroup {
                            button {
                                iconView(MaterialIcon.ARROW_DROP_DOWN)
                                title = "Expand"

                                disabledProperty.bind(traverserProperty.flatMapBinding { it.autoExpandProperty })

                                onClick { event ->
                                    if (event.shiftKey) {
                                        traverserProperty.value.clickExpand()
                                    } else {
                                        traverserProperty.value.clickFullExpand()
                                    }
                                }
                            }
                            button {
                                iconView(MaterialIcon.ARROW_DOWNWARD)
                                title = "Toggle auto expand"

                                classList.bind("active", traverserProperty.flatMapBinding { it.autoExpandProperty })

                                onClick {
                                    traverserProperty.value.autoExpandProperty.value = !traverserProperty.value.autoExpandProperty.value
                                }
                            }
                        }

                        button {
                            disabledProperty.bind(!traverserProperty.flatMapBinding { it.isNextEnabled })
                            iconView(MaterialIcon.CHEVRON_RIGHT)
                            onClick { event ->
                                traverserProperty.value.clickNextTrail()
                                event.stopPropagation()
                            }
                        }
                    }
                    boxView {
                        listFactory(traverserProperty.mapBinding { it.characteristicList }, factory = { characteristic ->
                            TraverserCharacteristicView(characteristic)
                        })
                    }
                }
            }
        }
    }
}

class TraverserEntryView(private val entry: ITraverserStateEntry) : ViewCollection<View>() {

    init {
        classList.bind("selected", entry.selected)

        boxView {
            button {
                disabledProperty.bind(!entry.isPreviousEnabled)
                iconView(MaterialIcon.CHEVRON_LEFT)

                onClick { event ->
                    entry.clickPreviousOption()
                    event.stopPropagation()
                }
            }

            textView(entry.visibleTitle)

            button {
                disabledProperty.bind(!entry.isNextEnabled)
                iconView(MaterialIcon.CHEVRON_RIGHT)

                onClick { event ->
                    entry.clickNextOption()
                    event.stopPropagation()
                }
            }

            classList.bind("hide-buttons", !entry.isPreviousEnabled and !entry.isNextEnabled)
        }

        bulletList {
            listFactory(entry.visibleDetails.mapBinding { it.toMutableList().asObservable() }, factory = { str: String ->
                ListItem(str)
            })
        }

        onClick {
            entry.select()
        }
    }
}

class TraverserCharacteristicView(private val item: CharacteristicItem) : ViewCollection<View>() {

    init {
        style {
            backgroundColor = item.color.toString()
        }
    }
}
