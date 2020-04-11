package de.roboplot.plotter.traverser

import de.roboplot.plotter.model.*
import de.roboplot.plotter.model.Target
import java.util.*
import kotlin.math.min

interface INavigator<NS> where NS : INavigatorState {
    val planet: LookupPlanet
    val seedState: NS
    fun drovePath(current: NS, path: Path, receivedPaths: List<Path>, receivedTargets: List<Target>): NS
    fun prepareLeaveNode(current: NS, location: Point, arrivingPath: Path): Pair<List<NS>, Boolean>
    fun prepareLeaveNodeInDirection(current: NavigatorState, location: Point, arrivingPath: Path, direction: Direction?, exploring: Boolean): NavigatorState?
    fun leavingNode(current: NS, direction: Direction): NS
}

class Navigator(override val planet: LookupPlanet) : INavigator<NavigatorState> {

    constructor(planet: Planet) : this(LookupPlanet(planet))

    override val seedState: NavigatorState = NavigatorState.getSeed(planet)

    override fun drovePath(current: NavigatorState, path: Path, receivedPaths: List<Path>, receivedTargets: List<Target>): NavigatorState =
            receivedPaths.fold(current.copy(pickedDirection = null, currentTarget = receivedTargets.lastOrNull()
                    ?: current.currentTarget).withPath(path)) { acc, newPath ->
                acc.withPath(newPath)
            }.restrictOpenExits(path.endPoint, planet.getLeavingDirections(path.endPoint))

    override fun prepareLeaveNode(current: NavigatorState, location: Point, arrivingPath: Path): Pair<List<NavigatorState>, Boolean> {
        val currentTargetLocation: Point? = current.currentTarget?.target
        val exploring: Boolean
        var paths: List<Direction?>
        if (currentTargetLocation == null) {
            exploring = true
            paths = exploreDijkstra(current, location).map(List<Direction>::firstOrNull).distinct()
            if (paths.isEmpty())
                paths = listOf(null)
        } else {
            paths = targetDijkstra(current, location, currentTargetLocation).map(List<Direction>::firstOrNull).distinct()
            exploring = paths.isEmpty()
            if (exploring) {
                paths = exploreDijkstra(current, location).map(List<Direction>::firstOrNull).distinct()
                if (paths.isEmpty())
                    paths = listOf(null)
            }
        }
        return Pair(paths.map { prepareLeaveNodeInDirection(current, location, arrivingPath, it, exploring) }, exploring)
    }

    override fun prepareLeaveNodeInDirection(current: NavigatorState, location: Point, arrivingPath: Path, direction: Direction?, exploring: Boolean): NavigatorState {
        if (direction == null) {
            return if (exploring) current.copy(exploring = exploring, explorationComplete = true)
            else current.copy(exploring = exploring, targetReached = true)
        }
        return current.copy(exploring = exploring, pickedDirection = direction)
    }

    override fun leavingNode(current: NavigatorState, direction: Direction): NavigatorState {
        return current
    }

    fun targetDijkstra(state: NavigatorState, start: Point, target: Point): List<List<Direction>> = targetDijkstra(state, start, target) { it.weight!!.toFloat() }

    fun targetDijkstra(state: NavigatorState, start: Point, target: Point, cost: (Path) -> Float): List<List<Direction>> =
            if (target !in state.paths.keys) emptyList()
            else dijkstra(state, start, { it == target }, cost).map { it.map(Pair<Direction, Point>::first) }

    fun exploreDijkstra(state: NavigatorState, start: Point): List<List<Direction>> = exploreDijkstra(state, start) { it.weight!!.toFloat() }

    fun exploreDijkstra(state: NavigatorState, start: Point, cost: (Path) -> Float): List<List<Direction>> {
        fun hasOpenExits(location: Point) = !state.openExits[location].isNullOrEmpty()
        val paths: List<List<Pair<Direction, Point>>> = if (hasOpenExits(start))
            listOf(emptyList())
        else
            dijkstra(state, start, ::hasOpenExits, cost)
        return paths.flatMap { oldPath ->
            state.openExits[oldPath.lastOrNull()?.second ?: start]!!
                    .map { oldPath.map(Pair<Direction, Point>::first) + it }
        }
    }

    //returns List of Paths
    //Paths are Lists of Steps
    //Steps are a Pair of Direction of step and location(Point) after step
    fun dijkstra(state: NavigatorState, start: Point, predicate: (Point) -> Boolean): List<List<Pair<Direction, Point>>> = dijkstra(state, start, predicate, { it.weight!!.toFloat() })

    fun dijkstra(state: NavigatorState, start: Point, predicate: (Point) -> Boolean, cost: (Path) -> Float, precision: Float = 0.01f): List<List<Pair<Direction, Point>>> {
        if (predicate(start)) return listOf(emptyList())
        val paths: Map<Point, Map<Direction, Path>> = state.paths
        if (start !in paths.keys) return emptyList()
        val visited: MutableSet<Point> = mutableSetOf()
        val targets: MutableSet<Point> = mutableSetOf()
        val notTargets: MutableSet<Point> = mutableSetOf()
        val predecessor: MutableMap<Point, Pair<Float, MutableSet<Pair<Direction, Point>>>> = mutableMapOf()
        predecessor[start] = Pair(0f, mutableSetOf())
        val prioQueue: PriorityQueue<Point> = PriorityQueue(kotlin.Comparator { o1, o2 -> compareValues(predecessor[o1]!!.first, predecessor[o2]!!.first) })
        var targetCutoff: Float = Float.POSITIVE_INFINITY
        prioQueue.add(start)
        while (!prioQueue.isEmpty()) {
            val current: Point = prioQueue.remove()
            val baseCost: Float = predecessor[current]!!.first
            visited.add(current)
            if (baseCost > targetCutoff) break
            paths[current]!!.forEach { currentPathEntry ->
                if (currentPathEntry.value.blocked) return@forEach
                val endPoint: Point = currentPathEntry.value.endPoint
                if (endPoint in visited) return@forEach
                val pathCost: Float = cost(currentPathEntry.value)
                if (pathCost < 0f) return@forEach
                val sumCost: Float = baseCost + pathCost
                val oldCost: Float = predecessor[endPoint]?.first ?: Float.POSITIVE_INFINITY
                val isUpdated: Boolean
                when {
                    sumCost < oldCost -> {
                        prioQueue.remove(endPoint)
                        predecessor[endPoint] = Pair(sumCost, mutableSetOf(Pair(currentPathEntry.key, current)))
                        prioQueue.add(endPoint)
                        isUpdated = true
                    }
                    sumCost < oldCost + precision -> {
                        val tmp = predecessor[endPoint]
                        if (tmp == null) {
                            predecessor[endPoint] = Pair(sumCost, mutableSetOf(Pair(currentPathEntry.key, current)))
                            prioQueue.add(endPoint)
                        } else
                            predecessor[endPoint]!!.second.add(Pair(currentPathEntry.key, current))
                        isUpdated = true
                    }
                    else -> isUpdated = false
                }
                if (isUpdated && endPoint !in notTargets) {
                    var isTarget: Boolean = true
                    if (endPoint !in targets) {
                        (if (predicate(endPoint).also { isTarget = it }) targets else notTargets).add(endPoint)
                    }
                    if (isTarget) {
                        targetCutoff = min(targetCutoff, sumCost + precision)
                    }
                }
            }
        }
        targets.removeIf { predecessor[it]!!.first > targetCutoff }

        fun backtrack(target: Point): List<List<Pair<Direction, Point>>> {
            val predSet: Set<Pair<Direction, Point>> = predecessor[target]?.second.orEmpty()
            if (predSet.isEmpty()) {
                return listOf(emptyList())
            }
            return predSet.flatMap { backtrack(it.second).map { path -> path + Pair(it.first, target) } }
        }

        return targets.flatMap(::backtrack)
    }
}