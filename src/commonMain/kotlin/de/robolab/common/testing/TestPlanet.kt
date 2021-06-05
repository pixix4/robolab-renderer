package de.robolab.common.testing

import de.robolab.client.traverser.ITraverser
import de.robolab.client.traverser.ITraverserState
import de.robolab.common.planet.PlanetPoint
import de.robolab.common.planet.PlanetDirection
import de.robolab.common.planet.PlanetPath
import de.robolab.common.planet.test.PlanetSubscribableRef
import de.robolab.common.utils.filterKeysNotNull

class TestPlanet(
    val explorationGoals: Set<PlanetPoint?>,
    val targetGoals: Set<PlanetPoint?>,
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

    val subscribersByIdentifier: Map<PlanetSubscribableRef, List<ITestableSubscriber>> =
        (tasks.values.flatten() + triggers + flags + signals)
            .filterIsInstance<ITestableSubscriber>().distinct()
            .groupBy(ITestableSubscriber::subscribable)

    fun onDrivePath(test: ITestRun, path: PlanetPath, newLocation: PlanetPoint) {
        val subscribers = subscribersByIdentifier[PlanetSubscribableRef.Node(newLocation.x, newLocation.y)] ?: return
        subscribers.forEach { with(it) { test.onTestableEntered() } }
    }

    fun onPickDirection(test: ITestRun, location: PlanetPoint, direction: PlanetDirection) {
        val subscribers = subscribersByIdentifier[PlanetSubscribableRef.Path(location.x, location.y, direction, true)] ?: return
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
