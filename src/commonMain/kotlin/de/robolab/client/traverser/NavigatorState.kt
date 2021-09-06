package de.robolab.client.traverser

import de.robolab.common.planet.*
import de.robolab.common.planet.utils.LookupPlanet

interface INavigatorState {
    val targetReached: Boolean
    val explorationComplete: Boolean
    val pickedDirection: PlanetDirection?
    val exploring: Boolean
    val currentTarget: PlanetTarget?
}

private fun addPath(paths: Map<PlanetPoint, Map<PlanetDirection, PlanetPath>>, path: PlanetPath): Map<PlanetPoint, Map<PlanetDirection, PlanetPath>> {
    val oldEntry: Map<PlanetDirection, PlanetPath>? = paths[path.source]
    return when {
        oldEntry == null -> paths + (path.source to mapOf(path.sourceDirection to path))
        else -> paths + (path.source to (oldEntry + (path.sourceDirection to path)))
    }
}

private fun addOpenExits(
    openExits: Map<PlanetPoint, Set<PlanetDirection>>,
    location: PlanetPoint
): Map<PlanetPoint, Set<PlanetDirection>> =
    if (location in openExits) openExits
    else openExits + (location to PlanetDirection.values().toSet())

private fun removeOpenExit(
    openExits: Map<PlanetPoint, Set<PlanetDirection>>,
    location: PlanetPoint,
    direction: PlanetDirection
): Map<PlanetPoint, Set<PlanetDirection>> {
    val oldDirections: Set<PlanetDirection> = openExits[location]
        ?: error("Cannot remove exit from non-existing set at $location")
    return openExits + (location to (oldDirections - direction))
}

private fun addExploredExit(
    exploredExits: Map<PlanetPoint, Set<PlanetDirection>>,
    location: PlanetPoint,
    direction: PlanetDirection
): Map<PlanetPoint, Set<PlanetDirection>> =
    exploredExits + (location to (exploredExits[location].orEmpty() + direction))

data class NavigatorState(
    val openExits: Map<PlanetPoint, Set<PlanetDirection>>,
    val exploredExits: Map<PlanetPoint, Set<PlanetDirection>>,
    val paths: Map<PlanetPoint, Map<PlanetDirection, PlanetPath>>,
    override val currentTarget: PlanetTarget?,
    override val explorationComplete: Boolean,
    override val targetReached: Boolean,
    override val pickedDirection: PlanetDirection?,
    override val exploring: Boolean
) : INavigatorState {

    fun withPath(path: PlanetPath) = copy(
        paths = addPath(addPath(paths, path), path.reversed()),
        openExits = removeOpenExit(
            removeOpenExit(
                addOpenExits(addOpenExits(openExits, path.source), path.target),
                path.source, path.sourceDirection
            ), path.target, path.targetDirection
        ),
        exploredExits = addExploredExit(
            addExploredExit(openExits, path.source, path.sourceDirection),
            path.target, path.targetDirection
        )
    )

    fun restrictOpenExits(location: PlanetPoint, directions: Set<PlanetDirection>): NavigatorState = copy(
        openExits = openExits + (location to (openExits[location]!!.intersect(directions)))
    )

    companion object Seed {
        fun getSeed(planet: LookupPlanet): NavigatorState = NavigatorState(
            explorationComplete = false,
            targetReached = false,
            pickedDirection = null,
            exploring = true,
            currentTarget = null,
            openExits = mapOf(planet.planet.startPoint.point to (planet.getLeavingDirections(planet.planet.startPoint.point)) - planet.planet.startPoint.orientation.opposite()),
            exploredExits = mapOf(planet.planet.startPoint.point to setOf(planet.planet.startPoint.orientation.opposite())),
            paths = mapOf(planet.planet.startPoint.point to mapOf(planet.planet.startPoint.orientation.opposite() to planet.planet.startPoint.path))
        )

        fun getSeed(planet: Planet): NavigatorState = getSeed(LookupPlanet(planet))
    }
}
