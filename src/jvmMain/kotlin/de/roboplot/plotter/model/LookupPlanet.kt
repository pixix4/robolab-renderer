package de.roboplot.plotter.model

fun Path.asReversed(): Path =
        if (startPoint == endPoint && startDirection == endDirection)
            this
        else
            copy(startPoint = endPoint, startDirection = endDirection,
                    endPoint = startPoint, endDirection = startDirection,
                    absoluteControlPoints = absoluteControlPoints?.asReversed())

fun Planet.getStartPath(): Path? =
        if (start == null) null
        else Path(Path.From.SERVER,
                start, startOrientation.opposite(),
                start, startOrientation.opposite(),
                -1, true)

class LookupPlanet(val planet: Planet) {
    private val visitFeatures: Map<Point, Pair<List<Path>, List<Target>>>

    init {
        val pathsByPoint: Map<Point, List<Path>> = planet.paths
                .flatMap { path -> path.exposure.map { it to path } }
                .groupBy(Pair<Point, Path>::first, Pair<Point, Path>::second)
        val targetsByPoint: Map<Point, List<Target>> = planet.targets
                .flatMap { target -> target.exposure.map { it to target } }
                .groupBy(Pair<Point, Target>::first, Pair<Point, Target>::second)
        visitFeatures = (pathsByPoint.keys + targetsByPoint.keys)
                .associateWith { Pair(pathsByPoint.getOrElse(it, ::emptyList), targetsByPoint.getOrElse(it, ::emptyList)) }
    }

    private val leaveFeatures: Map<Point, PathSelect> = planet.pathSelects.associateBy { it.point }

    private val paths: Map<Point, Map<Direction, Path>> = planet.paths
            .flatMap { listOf(it, it.asReversed()) }
            .groupBy(Path::startPoint)
            .mapValues { it.value.distinct().associateBy(Path::startDirection) }

    fun getVisitFeatures(point: Point): Pair<List<Path>, List<Target>>? = visitFeatures[point]
    fun getLeaveFeatures(point: Point): PathSelect? = getPathSelect(point)

    fun getPathSelect(point: Point): PathSelect? = leaveFeatures[point]
    fun getRevealedPaths(point: Point): List<Path> = visitFeatures[point]?.first.orEmpty()
    fun getSetTargets(point: Point): List<Target> = visitFeatures[point]?.second.orEmpty()
    fun getLastSetTarget(point: Point): Target? = visitFeatures[point]?.second?.lastOrNull()

    fun getPath(point: Point, direction: Direction): Path? = paths[point]?.get(direction)

    fun getPaths(point: Point): Collection<Path> = paths[point]?.values.orEmpty()

    fun getLeavingDirections(point: Point): Set<Direction> = paths[point]?.keys.orEmpty()
}