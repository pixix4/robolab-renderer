package de.robolab.client.app.controller.testing

import de.robolab.client.app.model.testing.FilterEntry
import de.robolab.client.app.model.testing.TestRunEntry
import de.robolab.common.testing.TestGoal
import de.robolab.common.testing.TestState
import de.robolab.common.testing.TestStatus
import de.robolab.common.testing.TestTraversal
import de.robolab.common.utils.tree.ISeededBranchProvider
import de.robolab.common.utils.tree.ObservableTreeFrontViewer
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableMutableList
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.property
import kotlin.time.Duration

class TestTraversalController(
    val traversal: TestTraversal<*>,
    localExpand: Boolean = false
) {
    @Suppress("UNCHECKED_CAST")
    private val _currentTestRuns: ObservableTreeFrontViewer<TestState<*>> =
        ObservableTreeFrontViewer.fromBranchProvider(traversal as ISeededBranchProvider<TestState<*>>, !localExpand)

    private val _observableStates: MutableMap<Int, ObservableProperty<TestState<*>>> = mutableMapOf()

    val currentTestRuns: ObservableMutableList<TestRunEntry> = observableListOf()

    private var nextRunNumber: Int = 1

    init {
        _currentTestRuns.onAddIndex {
            currentTestRuns.add(it.index, TestRunEntry(nextRunNumber++, it.element, traversal.planet))
        }
        _currentTestRuns.onRemoveIndex {
            currentTestRuns.removeAt(it.index)
        }
        _currentTestRuns.onSetIndex {
            val entry = currentTestRuns[it.index]
            entry.updateFromState(it.newElement)
            _observableStates[entry.number.value]?.set(it.newElement)
        }
        _currentTestRuns.onClear {
            currentTestRuns.clear()
            _observableStates.clear()
            nextRunNumber = 1
        }

        for (element in _currentTestRuns) {
            currentTestRuns.add(TestRunEntry(nextRunNumber++, element, traversal.planet))
        }
    }

    val isComplete: ObservableProperty<Boolean> = property(false)

    val goalFilters: ObservableList<FilterEntry.GoalFilterEntry> =
        observableListOf<FilterEntry.GoalFilterEntry>().apply {
            addAll(traversal.planet.targetGoals.map {
                FilterEntry.GoalFilterEntry(
                    if (it == null) throw IllegalArgumentException("Location for Target-Goal may not be null")
                    else TestGoal.Target(it)
                )
            } + (traversal.planet.explorationGoals.map {
                FilterEntry.GoalFilterEntry(
                    if (it == null) TestGoal.Explore
                    else TestGoal.ExploreCoordinate(it)
                )
            }))
        }

    val statusFilters: ObservableList<FilterEntry.StatusFilterEntry> =
        observableListOf<FilterEntry.StatusFilterEntry>().apply {
            addAll(TestStatus.values().map { FilterEntry.StatusFilterEntry(it) })
        }

    val filtersCollapsed: ObservableProperty<Boolean> = property(false)

    val title: ObservableValue<String> = property("Test of ${traversal.traverser.planet.planet.name}")

    private fun getObservableState(runNumber: Int): ObservableProperty<TestState<*>> {
        val prevState = _observableStates[runNumber]
        if (prevState != null) return prevState
        val index = currentTestRuns.indexOfFirst { it.number.value == runNumber }
        if (index < 0) throw NoSuchElementException("Run with Number $runNumber could not be found")
        val newState = property(_currentTestRuns.front[index])
        return _observableStates.getOrPut(runNumber) { newState }
    }

    fun createRunController(runNumber: Int) = TestRunController(
        this,
        runNumber,
        getObservableState(runNumber)
    )

    fun createRunController(entry: TestRunEntry): TestRunController = createRunController(entry.number.get())

    fun expandAllOnce() = _currentTestRuns.expandAll()

    suspend fun expandNextFullyAsync(depthFirst: Boolean, delay: Duration) =
        _currentTestRuns.expandNextFullyAsync(depthFirst, delay)

    suspend fun expandAllFullyAsync(depthFirst: Boolean, delay: Duration) =
        _currentTestRuns.expandAllFullyAsync(depthFirst, delay)
}