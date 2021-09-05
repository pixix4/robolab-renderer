package de.robolab.common.planet.utils

import de.robolab.common.planet.*

class LookupPlanet(val planet: Planet) {
    private val visitFeatures: Map<PlanetPoint, PlanetVisitFeature>

    init {
        val pathsByPoint = planet.paths
            .flatMap { path -> path.exposure.map { it to path } }
            .groupBy({ (exposure, _) ->
                exposure.planetPoint
            }, { (exposure, path) ->
                exposure.changes.applyUpdateOrPass(path)
            })
        val targetsByPoint: Map<PlanetPoint, List<PlanetTarget>> = planet.targets
            .flatMap { target ->
                target.exposure.map { exposure ->
                    exposure to target
                }
            }
            .groupBy(Pair<PlanetPoint, PlanetTarget>::first, Pair<PlanetPoint, PlanetTarget>::second)
        visitFeatures = (pathsByPoint.keys + targetsByPoint.keys)
            .associateWith {
                PlanetVisitFeature(
                    pathsByPoint.getOrElse(it, ::emptyList),
                    targetsByPoint.getOrElse(it, ::emptyList)
                )
            }
    }

    private val leaveFeatures: Map<PlanetPoint, PlanetPathSelect> = planet.pathSelects.associateBy { it.point }

    private val paths: Map<PlanetPoint, Map<PlanetDirection, PlanetPath>> = planet.paths
        .flatMap { listOf(it, it.reversed()) }
        .groupBy(PlanetPath::source)
        .mapValues { it.value.distinct().associateBy(PlanetPath::sourceDirection) }

    val reachablePaths: Set<PlanetPath>
    val reachablePoints: Set<PlanetPoint>

    init {
        val startPoint: PlanetPoint = planet.startPoint.point
        val tempReachablePaths: MutableSet<PlanetPath> = mutableSetOf()
        val tempReachablePoints: MutableSet<PlanetPoint> = mutableSetOf(startPoint)
        val pendingPoints: MutableSet<PlanetPoint> = mutableSetOf(startPoint)
        while (pendingPoints.isNotEmpty()) {
            val point: PlanetPoint = pendingPoints.first()
            pendingPoints.remove(point)
            for (path in getPaths(point)) {
                if (!tempReachablePaths.add(path)) continue
                if (tempReachablePoints.add(path.source))
                    pendingPoints.add(path.source)
                if (tempReachablePoints.add(path.target))
                    pendingPoints.add(path.target)
            }
        }

        reachablePoints = tempReachablePoints
        reachablePaths = tempReachablePaths
    }

    fun pointReachable(coordinate: PlanetPoint?): Boolean = coordinate != null && coordinate in reachablePoints

    fun getVisitFeatures(point: PlanetPoint): PlanetVisitFeature? = visitFeatures[point]
    fun getLeaveFeatures(point: PlanetPoint): PlanetPathSelect? = getPathSelect(point)

    fun getPathSelect(point: PlanetPoint): PlanetPathSelect? = leaveFeatures[point]
    fun getRevealedPaths(point: PlanetPoint): List<PlanetPath> = visitFeatures[point]?.revealedPaths.orEmpty()
    fun getSetTargets(point: PlanetPoint): List<PlanetTarget> = visitFeatures[point]?.setTargets.orEmpty()
    fun getLastSetTarget(point: PlanetPoint): PlanetTarget? = visitFeatures[point]?.setTargets?.lastOrNull()

    fun getPath(point: PlanetPoint, direction: PlanetDirection): PlanetPath? = paths[point]?.get(direction)

    fun getPaths(point: PlanetPoint): Collection<PlanetPath> = paths[point]?.values.orEmpty()

    fun getLeavingDirections(point: PlanetPoint): Set<PlanetDirection> = paths[point]?.keys.orEmpty()
}
