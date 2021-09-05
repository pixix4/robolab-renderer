package de.robolab.client.app.model.testing

import de.robolab.common.planet.PlanetDirection
import de.robolab.common.planet.PlanetPoint
import de.robolab.common.planet.test.PlanetTestGoal
import de.robolab.common.testing.TestPlanet
import de.robolab.common.testing.TestState
import de.robolab.common.testing.TestStatus
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.observeConst
import de.westermann.kobserve.property.property

data class TestRunEntry(
    val number: ObservableValue<Int>,
    val location: ObservableProperty<PlanetPoint>,
    val selectedDirection: ObservableProperty<PlanetDirection?>,
    val status: ObservableProperty<TestStatus>,
    val tasksCompleted: ObservableProperty<Int>,
    val tasksTotal: ObservableValue<Int>,
    val goal: ObservableProperty<PlanetTestGoal.GoalType?>
) {
    constructor(number: Int, state: TestState<*>, planet: TestPlanet) : this(
        number.observeConst(),
        property(state.traverserState.location),
        property(state.traverserState.nextDirection),
        property(state.status),
        property(state.completedTasks.size),
        planet.totalTaskCount.observeConst(),
        property(state.achievedGoalType)
    )

    fun updateFromState(state: TestState<*>) {
        location.set(state.traverserState.location)
        selectedDirection.set(state.traverserState.nextDirection)
        status.set(state.status)
        tasksCompleted.set(state.completedTasks.size)
        goal.set(state.achievedGoalType)
    }
}
