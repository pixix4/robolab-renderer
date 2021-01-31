package de.robolab.client.ui.views.boxes

import de.robolab.client.app.controller.TraverserBarController
import de.robolab.client.app.controller.UiController
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.traverser.CharacteristicItem
import de.robolab.client.app.model.traverser.ITraverserStateEntry
import de.robolab.client.renderer.events.KeyCode
import de.robolab.client.ui.adapter.toCommon
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
                        TraverserEntryView(entry, this)
                    })


                    allowFocus()
                    onKeyDown { event ->
                        when (event.toCommon()) {
                            KeyCode.ARROW_UP -> {
                                traverserProperty.value.keyUp()
                                event.preventDefault()
                                event.stopPropagation()
                            }
                            KeyCode.ARROW_DOWN -> {
                                traverserProperty.value.keyDown()
                                event.preventDefault()
                                event.stopPropagation()
                            }
                            KeyCode.ARROW_LEFT -> {
                                traverserProperty.value.keyLeft()
                                event.preventDefault()
                                event.stopPropagation()
                            }
                            KeyCode.ARROW_RIGHT -> {
                                traverserProperty.value.keyRight()
                                event.preventDefault()
                                event.stopPropagation()
                            }
                            else -> {
                            }
                        }
                    }
                }

                boxView("traverser-bar-footer") {
                    boxView("traverser-bar-trail") {
                        buttonGroup(true) {
                            button {
                                disabledProperty.bind(!traverserProperty.flatMapBinding { it.isPreviousEnabled })
                                iconView(MaterialIcon.CHEVRON_LEFT)

                                onClick { event ->
                                    traverserProperty.value.clickPreviousTrail()
                                    event.stopPropagation()
                                }
                            }
                        }

                        textView(traverserProperty.flatMapBinding { it.traverserTitle })

                        buttonGroup(true) {
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
                                    traverserProperty.value.autoExpandProperty.value =
                                        !traverserProperty.value.autoExpandProperty.value
                                }
                            }
                        }

                        buttonGroup(true) {
                            button {
                                disabledProperty.bind(!traverserProperty.flatMapBinding { it.isNextEnabled })
                                iconView(MaterialIcon.CHEVRON_RIGHT)
                                onClick { event ->
                                    traverserProperty.value.clickNextTrail()
                                    event.stopPropagation()
                                }
                            }
                        }
                    }
                    boxView {
                        listFactory(
                            traverserProperty.mapBinding { it.characteristicList },
                            factory = { characteristic ->
                                TraverserCharacteristicView(characteristic)
                            })
                    }
                }
            }
        }
    }
}

class TraverserEntryView(
    private val entry: ITraverserStateEntry,
    private val scrollView: BoxView
) : ViewCollection<View>() {


    private fun scrollIntoView2() {
        val viewTop = offsetTopTotal(0)
        val viewBottom = viewTop + clientHeight

        val parentTop = scrollView.scrollTop.toInt()
        val parentBottom = parentTop + scrollView.clientHeight

        val padding = 40

        if (viewTop - padding <= parentTop) {
            scrollView.scrollTo(
                top = viewTop - padding
            )
        } else if (viewBottom + padding >= parentBottom) {
            scrollView.scrollTo(
                top = viewBottom + padding - scrollView.clientHeight
            )
        }
    }


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
            listFactory(
                entry.visibleDetails.mapBinding { it.toMutableList().asObservable() },
                factory = { str: String ->
                    ListItem(str)
                })
        }

        onClick {
            entry.select()
        }


        entry.selected.onChange {
            if (entry.selected.value) {
                scrollIntoView2()
            }
        }

        if (entry.selected.value) {
            runAsync { scrollIntoView2() }
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
