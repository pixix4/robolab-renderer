package de.robolab.client.app.controller.testing

import de.robolab.client.traverser.navigation.Route
import de.robolab.common.planet.test.PlanetTestGoal
import de.robolab.common.testing.TestState
import de.robolab.common.testing.TestStatus
import de.robolab.common.testing.TestTraversal
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.observeConst
import de.westermann.kobserve.property.property

class TestRunController(
    val traversalController: TestTraversalController?,
    val number: Int,
    val state: ObservableValue<TestState<*>>,
    val traversal: TestTraversal<*>
) {

    constructor(traversalController: TestTraversalController, number: Int, state: ObservableValue<TestState<*>>) : this(
        traversalController,
        number,
        state,
        traversalController.traversal
    )

    val title: ObservableValue<String> = "Run #$number ".observeConst()
    val status: ObservableValue<TestStatus> = state.mapBinding(TestState<*>::status)
    val goal: ObservableValue<PlanetTestGoal.GoalType?> = state.mapBinding(TestState<*>::achievedGoalType)
    val route: ObservableValue<Route> = Route.empty(
        traversal.traverser.planet.planet.startPoint.point
    ).observeConst()

    fun createRouteRepresentation(): String = "Hier könnte ihre Route stehen"

    val completedTaskCount: ObservableValue<Int> = state.mapBinding { it.completedTasks.size }
    val totalTaskCount: ObservableValue<Int> = traversal.planet.totalTaskCount.observeConst()

    val taskBrowserCollapsed: ObservableProperty<Boolean> = property(false)
    val taskBrowserController: TaskBrowserController =
        TaskBrowserController(traversal.planet, state)
    val flagBrowserCollapsed: ObservableProperty<Boolean> = property(false)
    val flagBrowserController: FlagBrowserController =
        FlagBrowserController(traversal.planet, state)
}
