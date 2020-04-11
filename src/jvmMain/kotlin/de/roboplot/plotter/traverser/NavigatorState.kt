package de.roboplot.plotter.traverser

import de.roboplot.plotter.model.*
import de.roboplot.plotter.model.Target

interface INavigatorState {
    val targetReached: Boolean
    val explorationComplete: Boolean
    val pickedDirection: Direction?
    val exploring: Boolean
    val currentTarget: Target?
}

private fun addPath(paths: Map<Point, Map<Direction, Path>>, path: Path): Map<Point, Map<Direction, Path>> {
    val oldEntry: Map<Direction, Path>? = paths[path.startPoint]
    return when {
        oldEntry == null -> paths + (path.startPoint to mapOf(path.startDirection to path))
        path.startDirection !in oldEntry -> paths + (path.startPoint to (oldEntry + (path.startDirection to path)))
        else -> paths
    }
}

private fun addOpenExits(openExits: Map<Point, Set<Direction>>, location: Point): Map<Point, Set<Direction>> =
        if (location in openExits) openExits
        else openExits + (location to Direction.values().toSet())

private fun removeOpenExit(openExits: Map<Point, Set<Direction>>, location: Point, direction: Direction): Map<Point, Set<Direction>> {
    val oldDirections: Set<Direction> = openExits[location]
            ?: error("Cannot remove exit from non-existing set at $location")
    return openExits + (location to (oldDirections - direction))
}

private fun addExploredExit(exploredExits: Map<Point, Set<Direction>>, location: Point, direction: Direction): Map<Point, Set<Direction>> =
        exploredExits + (location to (exploredExits[location].orEmpty() + direction))

data class NavigatorState(
        val openExits: Map<Point, Set<Direction>>,
        val exploredExits: Map<Point, Set<Direction>>,
        val paths: Map<Point, Map<Direction, Path>>,
        override val currentTarget: Target?,
        override val explorationComplete: Boolean,
        override val targetReached: Boolean,
        override val pickedDirection: Direction?,
        override val exploring: Boolean
) : INavigatorState {

    fun withPath(path: Path) = copy(
            paths = addPath(addPath(paths, path), path.asReversed()),
            openExits = removeOpenExit(removeOpenExit(
                    addOpenExits(addOpenExits(openExits, path.startPoint), path.endPoint),
                    path.startPoint, path.startDirection), path.endPoint, path.endDirection),
            exploredExits = addExploredExit(
                    addExploredExit(openExits, path.startPoint, path.startDirection),
                    path.endPoint, path.endDirection)
    )

    fun restrictOpenExits(location: Point, directions: Set<Direction>): NavigatorState = copy(
            openExits = openExits + (location to (openExits[location]!!.intersect(directions)))
    )

    companion object Seed {
        fun getSeed(planet: LookupPlanet): NavigatorState = NavigatorState(
                explorationComplete = false,
                targetReached = false,
                pickedDirection = null,
                exploring = true,
                currentTarget = null,
                openExits = mapOf(planet.planet.start!! to (planet.getLeavingDirections(planet.planet.start)) - planet.planet.startOrientation.opposite()),
                exploredExits = mapOf(planet.planet.start to setOf(planet.planet.startOrientation.opposite())),
                paths = mapOf(planet.planet.start to mapOf(planet.planet.startOrientation.opposite() to planet.planet.getStartPath()!!))
        )

        fun getSeed(planet: Planet): NavigatorState = getSeed(LookupPlanet(planet))
    }
}