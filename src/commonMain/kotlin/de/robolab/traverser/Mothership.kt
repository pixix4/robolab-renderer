package de.robolab.traverser

import de.robolab.planet.*

interface IMothership<T> where T : IMothershipState {
    val planet: LookupPlanet
    val seedState: T
    fun drivePath(current: T, path: Path): T
    fun pickDirection(current: T, direction: Direction): T
    fun peekForceDirection(current: T): Direction?
}


class Mothership(override val planet: LookupPlanet) : IMothership<MothershipState> {

    constructor(planet: Planet) : this(LookupPlanet(planet))

    override val seedState: MothershipState = MothershipState.getSeed(planet)

    override fun drivePath(current: MothershipState, path: Path): MothershipState = with(current) {
        var result: MothershipState = copy(
                drivenPath = path,
                visitedLocations = visitedLocations + path.target,
                currentLocation = path.target,
                newPaths = emptyList(),
                newTargets = emptyList(),
                selectedDirection = null,
                forcedDirection = null,
                isStart = false,
                beforePoint = true
        )
        if (visitedLocations.size <= result.visitedLocations.size) { //added new endPoint
            val features: Pair<List<Path>, List<TargetPoint>>? = planet.getVisitFeatures(path.target)
            if (features != null)
                result = result.copy(
                        sentPaths = sentPaths + features.first,
                        newPaths = features.first - sentPaths,
                        sentTargets = sentTargets + features.second,
                        newTargets = features.second - sentTargets
                )
        }
        return@with result
    }

    override fun pickDirection(current: MothershipState, direction: Direction): MothershipState = with(current) {
        var currentPathSelect: PathSelect? = planet.getPathSelect(currentLocation)
        if (currentPathSelect != null && currentPathSelect in sentPathSelects)
            currentPathSelect = null
        return@with copy(
                beforePoint = false,
                selectedDirection = direction,
                sentPathSelects = if (currentPathSelect != null) sentPathSelects + currentPathSelect else sentPathSelects,
                forcedDirection = currentPathSelect?.direction
        )
    }

    override fun peekForceDirection(current: MothershipState): Direction? = with(current) {
        var currentPathSelect: PathSelect? = planet.getPathSelect(currentLocation)
        if (currentPathSelect != null && currentPathSelect in sentPathSelects)
            return@with null
        else
            return@with currentPathSelect?.direction
    }

}