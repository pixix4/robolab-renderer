package de.robolab.client.ui.views.boxes

import de.robolab.client.app.controller.testing.TestTraversalController
import de.robolab.client.app.controller.UiController
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.ui.views.utils.buttonGroup
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.utils.runAsync
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.toggle
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.*
import de.westermann.kwebview.extra.listFactory
import de.westermann.kwebview.extra.scrollBoxView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class InfoBarFileTestView(
    private val testProperty: ObservableValue<TestTraversalController>,
    private val uiController: UiController
) : ViewCollection<View>() {

    init {
        scrollBoxView {
            uiController.infoBarVisibleProperty.onChange {
                runAsync {
                    updateScrollBox()
                }
            }
            resizeBox(0.3) {
                textView(testProperty.flatMapBinding { it.title })

                boxView {
                    textView("Goals")

                    boxView {
                        classList += "info-bar-test-filter-list"
                        listFactory(testProperty.mapBinding { it.goalFilters }, { entry ->
                            BoxView().apply {
                                classList.bind("active", entry.active)
                                textView(entry.content)
                                textView(entry.matchingCount)

                                onClick {
                                    entry.active.toggle()
                                }
                            }
                        })
                    }
                }

                boxView {
                    textView("Status")

                    boxView {
                        classList += "info-bar-test-filter-list"
                        listFactory(testProperty.mapBinding { it.statusFilters }, { entry ->
                            runAsync {
                                updateScrollBox()
                            }
                            BoxView().apply {
                                classList.bind("active", entry.active)
                                textView(entry.content)
                                textView(entry.matchingCount)

                                onClick {
                                    entry.active.toggle()
                                }
                            }
                        })
                    }
                }
            }
            resizeBox(0.7) {
                onWheel {
                    if (it.deltaY < 0) {
                        testProperty.value.stickToTableBottom.value = false
                    }
                }
                boxView("info-bar-test-table-box") {
                    buttonGroup(true) {
                        button {
                            iconView(MaterialIcon.VERTICAL_ALIGN_BOTTOM)
                            classList.bind("active", testProperty.flatMapBinding { it.stickToTableBottom })
                            onClick {
                                testProperty.value.stickToTableBottom.toggle()
                            }
                        }
                        button("Expand next") {
                            onClick {
                                GlobalScope.launch {
                                    testProperty.value.expandNextFullyAsync(
                                        true,
                                        PreferenceStorage.traverserDelay.toDuration(DurationUnit.MILLISECONDS)
                                    )
                                }
                            }
                        }
                        button("Expand all") {
                            onClick {
                                GlobalScope.launch {
                                    testProperty.value.expandAllFullyAsync(
                                        true,
                                        PreferenceStorage.traverserDelay.toDuration(DurationUnit.MILLISECONDS)
                                    )
                                }
                            }
                        }
                    }
                    table("info-bar-group-view-content") {
                        thead {
                            row {
                                head {
                                    textView("ID")
                                }
                                head {
                                    textView("Location")
                                }
                                head {
                                    textView("Status")
                                }
                                head {
                                    textView("Tasks")
                                }
                                head {
                                    textView("Goal")
                                }
                            }
                        }
                        tbody {
                            listFactory(testProperty.mapBinding { it.currentTestRuns }, { entry ->
                                TableRow().apply {
                                    if (testProperty.value.stickToTableBottom.value) {
                                        runAsync {
                                            scrollIntoView()
                                        }
                                    }

                                    cell {
                                        textView(entry.number)
                                    }
                                    cell {
                                        textView(entry.location.join(entry.selectedDirection) { location, direction ->
                                            buildString {
                                                append(location.x.toString().padStart(4))
                                                append(", ")
                                                append(location.y.toString().padStart(4))
                                                if (direction != null) {
                                                    append(", ")
                                                    append(direction.name.first().toUpperCase())
                                                }
                                            }
                                        })
                                    }
                                    cell {
                                        textView(entry.status)
                                    }
                                    cell {
                                        textView(entry.tasksCompleted.join(entry.tasksTotal) { completed, total ->
                                            "${completed.toString().padStart(3)} / ${total.toString().padStart(3)}"
                                        })
                                    }
                                    cell {
                                        textView(entry.goal.mapBinding { it ?: "" })
                                    }
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}
