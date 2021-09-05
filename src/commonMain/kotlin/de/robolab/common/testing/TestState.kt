package de.robolab.common.testing

import de.robolab.client.traverser.ITraverser
import de.robolab.client.traverser.ITraverserState
import de.robolab.client.traverser.Traverser
import de.robolab.client.traverser.TraverserState
import de.robolab.common.planet.test.PlanetTestGoal

data class TestState<TS>(
    val signalPhases: Map<TestSignalGroup, TestSignalGroup.Phase>,
    val completedTasks: Set<TestTask>,
    val activeFlags: Set<TestSignalFlag>,
    val status: TestStatus,
    val statusMessage: String?,
    val achievedGoalType: PlanetTestGoal.GoalType?,
    val traverserState: TS,
    val signalsByPhase: Map<TestSignalGroup.Phase, Set<TestSignalGroup>> = signalPhases.entries
        .groupBy({ it.value }, { it.key }).mapValues { it.value.toSet() },
) where TS : ITraverserState<TS> {
    companion object {
        fun <TS : ITraverserState<TS>> empty(planet: TestPlanet, traverser: ITraverser<TS>): TestState<TS> = TestState(
            planet.signals.values.associateWith { TestSignalGroup.Phase.Pending },
            emptySet(),
            planet.flags.filter(TestSignalFlag::defaultActive).toSet(),
            TestStatus.Running,
            null,
            null,
            traverser.seed
        )
    }
}