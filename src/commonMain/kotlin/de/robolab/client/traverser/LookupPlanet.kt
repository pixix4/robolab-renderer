package de.robolab.client.traverser

import de.robolab.common.planet.*

fun Planet.getStartPath(): Path? =
        if (startPoint == null) null
        else Path(startPoint.point, startPoint.orientation.opposite(), startPoint.point, startPoint.orientation.opposite(),
                -1, emptySet(), emptyList(), false)

fun Planet.asUnexplored(name: String = this.name): Planet = Planet(name, startPoint, bluePoint, emptyList(), emptyList(), emptyList(), emptyList())

class LookupPlanet(val planet: Planet) {
    private val visitFeatures: Map<Coordinate, Pair<List<Path>, List<TargetPoint>>>

    init {
        val pathsByPoint = planet.pathList
                .flatMap { path -> path.exposure.map { it to path } }
                .groupBy(Pair<Coordinate, Path>::first, Pair<Coordinate, Path>::second)
        val targetsByPoint: Map<Coordinate, List<TargetPoint>> = planet.targetList
                .map { target -> target.exposure to target }
                .groupBy(Pair<Coordinate, TargetPoint>::first, Pair<Coordinate, TargetPoint>::second)
        visitFeatures = (pathsByPoint.keys + targetsByPoint.keys)
                .associateWith { Pair(pathsByPoint.getOrElse(it, ::emptyList), targetsByPoint.getOrElse(it, ::emptyList)) }
    }

    private val leaveFeatures: Map<Coordinate, PathSelect> = planet.pathSelectList.associateBy { it.point }

    private val paths: Map<Coordinate, Map<Direction, Path>> = planet.pathList
            .flatMap { listOf(it, it.reversed()) }
            .groupBy(Path::source)
            .mapValues { it.value.distinct().associateBy(Path::sourceDirection) }

    val reachablePaths: Set<Path>
    val reachablePoints: Set<Coordinate>

    init {
        val startPoint: Coordinate? = planet.startPoint?.point
        if (startPoint == null) {
            reachablePaths = emptySet()
            reachablePoints = emptySet()
        } else {
            val tempReachablePaths: MutableSet<Path> = mutableSetOf()
            val tempReachablePoints: MutableSet<Coordinate> = mutableSetOf(startPoint)
            val pendingPoints: MutableSet<Coordinate> = mutableSetOf(startPoint)
            while (pendingPoints.isNotEmpty()) {
                val point: Coordinate = pendingPoints.first()
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
    }

    fun pointReachable(coordinate: Coordinate?): Boolean = coordinate != null && coordinate in reachablePoints

    fun getVisitFeatures(point: Coordinate): Pair<List<Path>, List<TargetPoint>>? = visitFeatures[point]
    fun getLeaveFeatures(point: Coordinate): PathSelect? = getPathSelect(point)

    fun getPathSelect(point: Coordinate): PathSelect? = leaveFeatures[point]
    fun getRevealedPaths(point: Coordinate): List<Path> = visitFeatures[point]?.first.orEmpty()
    fun getSetTargets(point: Coordinate): List<TargetPoint> = visitFeatures[point]?.second.orEmpty()
    fun getLastSetTarget(point: Coordinate): TargetPoint? = visitFeatures[point]?.second?.lastOrNull()

    fun getPath(point: Coordinate, direction: Direction): Path? = paths[point]?.get(direction)

    fun getPaths(point: Coordinate): Collection<Path> = paths[point]?.values.orEmpty()

    fun getLeavingDirections(point: Coordinate): Set<Direction> = paths[point]?.keys.orEmpty()
}
