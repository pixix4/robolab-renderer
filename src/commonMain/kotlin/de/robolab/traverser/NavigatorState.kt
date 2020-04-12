package de.robolab.traverser

import de.robolab.planet.*

interface INavigatorState {
    val targetReached: Boolean
    val explorationComplete: Boolean
    val pickedDirection: Direction?
    val exploring: Boolean
    val currentTarget: TargetPoint?
}

private fun addPath(paths: Map<Coordinate, Map<Direction, Path>>, path: Path): Map<Coordinate, Map<Direction, Path>> {
    val oldEntry: Map<Direction, Path>? = paths[path.source]
    return when {
        oldEntry == null -> paths + (path.source to mapOf(path.sourceDirection to path))
        path.sourceDirection !in oldEntry -> paths + (path.source to (oldEntry + (path.sourceDirection to path)))
        else -> paths
    }
}

private fun addOpenExits(openExits: Map<Coordinate, Set<Direction>>, location: Coordinate): Map<Coordinate, Set<Direction>> =
        if (location in openExits) openExits
        else openExits + (location to Direction.values().toSet())

private fun removeOpenExit(openExits: Map<Coordinate, Set<Direction>>, location: Coordinate, direction: Direction): Map<Coordinate, Set<Direction>> {
    val oldDirections: Set<Direction> = openExits[location]
            ?: error("Cannot remove exit from non-existing set at $location")
    return openExits + (location to (oldDirections - direction))
}

private fun addExploredExit(exploredExits: Map<Coordinate, Set<Direction>>, location: Coordinate, direction: Direction): Map<Coordinate, Set<Direction>> =
        exploredExits + (location to (exploredExits[location].orEmpty() + direction))

data class NavigatorState(
        val openExits: Map<Coordinate, Set<Direction>>,
        val exploredExits: Map<Coordinate, Set<Direction>>,
        val paths: Map<Coordinate, Map<Direction, Path>>,
        override val currentTarget: TargetPoint?,
        override val explorationComplete: Boolean,
        override val targetReached: Boolean,
        override val pickedDirection: Direction?,
        override val exploring: Boolean
) : INavigatorState {

    fun withPath(path: Path) = copy(
            paths = addPath(addPath(paths, path), path.asReversed()),
            openExits = removeOpenExit(removeOpenExit(
                    addOpenExits(addOpenExits(openExits, path.source), path.target),
                    path.source, path.sourceDirection), path.target, path.targetDirection),
            exploredExits = addExploredExit(
                    addExploredExit(openExits, path.source, path.sourceDirection),
                    path.target, path.targetDirection)
    )

    fun restrictOpenExits(location: Coordinate, directions: Set<Direction>): NavigatorState = copy(
            openExits = openExits + (location to (openExits[location]!!.intersect(directions)))
    )

    companion object Seed {
        fun getSeed(planet: LookupPlanet): NavigatorState = NavigatorState(
                explorationComplete = false,
                targetReached = false,
                pickedDirection = null,
                exploring = true,
                currentTarget = null,
                openExits = mapOf(planet.planet.startPoint?.point!! to (planet.getLeavingDirections(planet.planet.startPoint.point)) - planet.planet.startPoint.orientation.opposite()),
                exploredExits = mapOf(planet.planet.startPoint.point to setOf(planet.planet.startPoint.orientation.opposite())),
                paths = mapOf(planet.planet.startPoint.point to mapOf(planet.planet.startPoint.orientation.opposite() to planet.planet.getStartPath()!!))
        )

        fun getSeed(planet: Planet): NavigatorState = getSeed(LookupPlanet(planet))
    }
}
