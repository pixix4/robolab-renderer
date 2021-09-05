package de.robolab.client.traverser

import de.robolab.client.traverser.navigation.Dijkstra
import de.robolab.client.traverser.navigation.Route
import de.robolab.client.traverser.navigation.getNeighbours
import de.robolab.common.planet.*
import de.robolab.common.planet.utils.LookupPlanet
import de.robolab.common.utils.Logger

interface INavigator<NS> where NS : INavigatorState {
    val planet: LookupPlanet
    val seedState: NS
    fun drovePath(
        current: NS,
        path: PlanetPath,
        receivedPaths: List<PlanetPath>,
        receivedTargets: List<PlanetTarget>
    ): NS

    fun prepareLeaveNode(current: NS, location: PlanetPoint, arrivingPath: PlanetPath): Pair<List<NS>, Boolean>
    fun prepareLeaveNodeInDirection(
        current: NavigatorState,
        location: PlanetPoint,
        arrivingPath: PlanetPath,
        direction: PlanetDirection?,
        exploring: Boolean
    ): NavigatorState?

    fun leavingNode(current: NS, direction: PlanetDirection): NS
}

class Navigator(override val planet: LookupPlanet) : INavigator<NavigatorState> {

    constructor(planet: Planet) : this(LookupPlanet(planet))

    override val seedState: NavigatorState = NavigatorState.getSeed(planet)

    override fun drovePath(
        current: NavigatorState,
        path: PlanetPath,
        receivedPaths: List<PlanetPath>,
        receivedTargets: List<PlanetTarget>
    ): NavigatorState = receivedPaths
        .flatMap { (if (it.blocked && !it.isOneWayPath) it.splitPath().toList() else listOf(it)) }
        .fold(
            current.copy(
                pickedDirection = null, currentTarget = receivedTargets.lastOrNull()
                    ?: current.currentTarget
            ).withPath(path)
        ) { acc, newPath ->
            acc.withPath(newPath)
        }.restrictOpenExits(path.target, planet.getLeavingDirections(path.target))

    override fun prepareLeaveNode(
        current: NavigatorState,
        location: PlanetPoint,
        arrivingPath: PlanetPath
    ): Pair<List<NavigatorState>, Boolean> {
        val currentTargetLocation: PlanetPoint? = current.currentTarget?.point
        val exploring: Boolean
        var paths: List<PlanetDirection?>
        if (currentTargetLocation == null) {
            exploring = true
            paths = exploreDijkstra(current, location).map { it.firstStep?.direction }.distinct()
            if (paths.isEmpty())
                paths = listOf(null)
        } else {
            paths =
                targetDijkstra(current, location, currentTargetLocation).map { it.firstStep?.direction }
                    .distinct()
            exploring = paths.isEmpty()
            if (exploring) {
                paths = exploreDijkstra(current, location).map { it.firstStep?.direction }.distinct()
                if (paths.isEmpty())
                    paths = listOf(null)
            }
        }
        return Pair(
            paths.map { prepareLeaveNodeInDirection(current, location, arrivingPath, it, exploring) },
            exploring
        )
    }

    override fun prepareLeaveNodeInDirection(
        current: NavigatorState,
        location: PlanetPoint,
        arrivingPath: PlanetPath,
        direction: PlanetDirection?,
        exploring: Boolean
    ): NavigatorState {
        if (direction == null) {
            return if (exploring) current.copy(exploring = exploring, explorationComplete = true)
            else current.copy(exploring = exploring, targetReached = true)
        }
        return current.copy(exploring = exploring, pickedDirection = direction)
    }

    override fun leavingNode(current: NavigatorState, direction: PlanetDirection): NavigatorState {
        return current
    }

    fun targetDijkstra(state: NavigatorState, start: PlanetPoint, target: PlanetPoint): List<Route> =
        Dijkstra.shortestPaths(start, state::getNeighbours, target)

    fun exploreDijkstra(state: NavigatorState, start: PlanetPoint): List<Route> {
        fun hasOpenExits(location: PlanetPoint) = !state.openExits[location].isNullOrEmpty()
        val paths: List<Route> = if (hasOpenExits(start))
            listOf(Route.empty(start))
        else
            Dijkstra.shortestPaths(start, state::getNeighbours, matchPredicate = ::hasOpenExits)
        return paths.flatMap { route ->
            state.openExits[route.end]!!.map { route + Route.EndStep(it, route.end) }
        }
    }
}
