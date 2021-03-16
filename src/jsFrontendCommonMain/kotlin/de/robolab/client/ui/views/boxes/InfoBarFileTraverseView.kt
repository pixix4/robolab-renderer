package de.robolab.client.ui.views.boxes

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.details.InfoBarFileTraverse
import de.robolab.client.app.model.traverser.CharacteristicItem
import de.robolab.client.app.model.traverser.ITraverserStateEntry
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.renderer.events.KeyCode
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.adapter.getKeyCode
import de.robolab.client.utils.runAsync
import de.westermann.kobserve.and
import de.westermann.kobserve.list.asObservable
import de.westermann.kobserve.not
import de.westermann.kobserve.property.mapBinding
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.*
import de.westermann.kwebview.extra.listFactory
import de.westermann.kwebview.extra.scrollBoxView

class InfoBarFileTraverseView(
    private val viewModel: InfoBarFileTraverse,
) : ViewCollection<View>() {

    init {
        viewModel.traverse()

        scrollBoxView {
            viewModel.uiController.infoBarVisibleProperty.onChange {
                runAsync {
                    updateScrollBox()
                }
            }
            resizeBox(1.0) {

                boxView("traverser-bar-body") {
                    listFactory(viewModel.entryList, factory = { entry ->
                        TraverserEntryView(entry, this)
                    })

                    allowFocus()
                    onKeyDown { event ->
                        when (event.getKeyCode()) {
                            KeyCode.ARROW_UP -> {
                                viewModel.traverserProperty.value?.keyUp()
                                event.preventDefault()
                                event.stopPropagation()
                            }
                            KeyCode.ARROW_DOWN -> {
                                viewModel.traverserProperty.value?.keyDown()
                                event.preventDefault()
                                event.stopPropagation()
                            }
                            KeyCode.ARROW_LEFT -> {
                                viewModel.traverserProperty.value?.keyLeft()
                                event.preventDefault()
                                event.stopPropagation()
                            }
                            KeyCode.ARROW_RIGHT -> {
                                viewModel.traverserProperty.value?.keyRight()
                                event.preventDefault()
                                event.stopPropagation()
                            }
                            else -> {
                            }
                        }
                    }
                }

                boxView {
                    listFactory(
                        viewModel.characteristicList,
                        factory = { characteristic ->
                            TraverserCharacteristicView(characteristic)
                        })
                }
            }
        }
    }

    companion object : ViewFactory {
        override fun matches(viewModel: ViewModel): Boolean {
            return viewModel is InfoBarFileTraverse
        }

        override fun create(viewModel: ViewModel): View {
            return InfoBarFileTraverseView(viewModel as InfoBarFileTraverse)
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
}
