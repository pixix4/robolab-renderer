package de.robolab.common.testing

import de.robolab.client.traverser.ITraverser
import de.robolab.client.traverser.ITraverserState
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Direction
import de.robolab.common.planet.Path
import de.robolab.common.planet.SubscribableIdentifier
import de.robolab.common.utils.filterKeysNotNull
import de.robolab.common.utils.filterValuesNotNull

class TestPlanet(
    val explorationGoals: Set<Coordinate?>,
    val targetGoals: Set<Coordinate?>,
    val tasks: Map<TestSignal?, Set<TestTask>>,
    val triggers: List<TestTrigger>,
    val flags: List<TestSignalFlag>,
    val signals: Map<TestSignal, TestSignalGroup>,
) {

    val totalTaskCount: Int = tasks.flatMap { it.value }.size
    val orderedTasks: Map<TestSignal.Ordered, Set<TestTask>> =
        tasks.mapKeys { it.key as? TestSignal.Ordered }.filterKeysNotNull()
    val unorderedTasks: Map<TestSignal.Unordered, Set<TestTask>> =
        tasks.mapKeys { it.key as? TestSignal.Unordered }.filterKeysNotNull()
    val globalTasks: Set<TestTask> = tasks.getOrElse(null, ::emptySet)

    val subscribersByIdentifier: Map<SubscribableIdentifier<*>, List<ITestableSubscriber>> =
        (tasks.values.flatten() + triggers + flags + signals)
            .filterIsInstance<ITestableSubscriber>().distinct()
            .groupBy(ITestableSubscriber::subscribable)

    fun onDrivePath(test: ITestRun, path: Path, newLocation: Coordinate) {
        val subscribers = subscribersByIdentifier[SubscribableIdentifier.Node(newLocation)] ?: return
        subscribers.forEach { with(it) { test.onTestableEntered() } }
    }

    fun onPickDirection(test: ITestRun, location: Coordinate, direction: Direction) {
        val subscribers = subscribersByIdentifier[SubscribableIdentifier.Path(location, direction)] ?: return
        subscribers.forEach { with(it) { test.onTestableEntered() } }
    }

    fun triggerSubscribers(test: ITestRun, state: ITraverserState<*>) {
        if (state.beforePoint)
            onDrivePath(test, state.mothershipState.drivenPath, state.location)
        else {
            val nextDirection = state.nextDirection
            if (nextDirection != null) onPickDirection(test, state.location, nextDirection)
            else throw IllegalStateException("No direction selected when leaving point")
        }
    }
}

fun <TS> TestPlanet.testWith(traverser: ITraverser<TS>): TestTraversal<TS> where TS : ITraverserState<TS> =
    TestTraversal(this, traverser)
