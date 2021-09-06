package de.robolab.client.traverser

import de.robolab.common.planet.*
import de.robolab.common.planet.utils.LookupPlanet
import de.robolab.common.planet.utils.PlanetVisitFeature

interface IMothership<T> where T : IMothershipState {
    val planet: LookupPlanet
    val seedState: T
    fun drivePath(current: T, path: PlanetPath): T
    fun pickDirection(current: T, direction: PlanetDirection): T
    fun peekForceDirection(current: T): PlanetDirection?
    fun infoTestTargetReached(current: T): String = if (testTargetReached(current)) "correct" else "incorrect"
    fun infoTestExplorationComplete(current: T): String = if (testExplorationComplete(current)) "correct" else "incorrect"
    fun testTargetReached(current: T): Boolean
    fun testExplorationComplete(current: T): Boolean
}


class Mothership(override val planet: LookupPlanet) : IMothership<MothershipState> {

    constructor(planet: Planet) : this(LookupPlanet(planet))

    override val seedState: MothershipState = MothershipState.getSeed(planet)

    override fun drivePath(current: MothershipState, path: PlanetPath): MothershipState = with(current) {
        var result: MothershipState = copy(
                drivenPath = path,
                visitedLocations = visitedLocations + path.target,
                currentLocation = path.target,
                sentPaths = sentPaths + path,
                newPaths = emptyList(),
                newTargets = emptyList(),
                selectedDirection = null,
                forcedDirection = null,
                isStart = false,
                beforePoint = true
        )
        if (visitedLocations.size <= result.visitedLocations.size) { //added new endPoint
            val features: PlanetVisitFeature? = planet.getVisitFeatures(path.target)
            if (features != null)
                result = result.copy(
                        sentPaths = sentPaths + (features.revealedPaths + path),
                        newPaths = features.revealedPaths - sentPaths,
                        sentTargets = sentTargets + features.setTargets,
                        newTargets = features.setTargets - sentTargets,
                        currentTarget = (features.setTargets - sentTargets).lastOrNull() ?: currentTarget
                )
        }
        return@with result
    }

    override fun pickDirection(current: MothershipState, direction: PlanetDirection): MothershipState = with(current) {
        var currentPathSelect: PlanetPathSelect? = planet.getPathSelect(currentLocation)
        if (currentPathSelect != null && currentPathSelect in sentPathSelects)
            currentPathSelect = null
        return@with copy(
                beforePoint = false,
                selectedDirection = direction,
                sentPathSelects = if (currentPathSelect != null) sentPathSelects + currentPathSelect else sentPathSelects,
                forcedDirection = currentPathSelect?.direction
        )
    }

    override fun peekForceDirection(current: MothershipState): PlanetDirection? = with(current) {
        var currentPathSelect: PlanetPathSelect? = planet.getPathSelect(currentLocation)
        if (currentPathSelect != null && currentPathSelect in sentPathSelects)
            return@with null
        else
            return@with currentPathSelect?.direction
    }

    override fun testExplorationComplete(current: MothershipState): Boolean =
            (!planet.pointReachable(current.currentTarget?.point)) &&
                    planet.reachablePaths.all { it in current.sentPaths || it.reversed() in current.sentPaths }

    override fun testTargetReached(current: MothershipState): Boolean =
            current.currentLocation == current.currentTarget?.point

    override fun infoTestTargetReached(current: MothershipState): String =
            if (current.currentLocation == current.currentTarget?.point)
                "correct"
            else
                "incorrect; target: ${current.currentTarget?.point}, location ${current.currentLocation}"

    override fun infoTestExplorationComplete(current: MothershipState): String {
        val missingPaths: List<PlanetPath> = planet.reachablePaths.filter { it !in current.sentPaths && it.reversed() !in current.sentPaths }
        return if (missingPaths.isEmpty()) {
            if (planet.pointReachable(current.currentTarget?.point))
                "maybe incorrect; target might be reachable: ${current.currentTarget?.point}"
            else
                "correct"
        } else
            "incorrect; missing paths: $missingPaths"
    }

}
