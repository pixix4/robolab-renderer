package de.robolab.client.traverser.navigation

import de.robolab.common.planet.LookupPlanet
import de.robolab.client.traverser.NavigatorState
import de.robolab.common.utils.PriorityQueue
import de.robolab.client.traverser.navigation.Route.Companion.toStartStep
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Direction
import de.robolab.common.planet.Path
import de.robolab.common.planet.Planet
import kotlin.math.min

typealias NeighboursFunction = (location: Coordinate) -> List<Route.EndStep>

object Dijkstra {
    inline fun createField(
        start: Coordinate,
        neighbours: NeighboursFunction,
        equalityRange: Float = 0.01f
    ): NavigationField = createField(start, neighbours, Float.POSITIVE_INFINITY, equalityRange)

    inline fun createField(
        start: Coordinate,
        neighbours: NeighboursFunction,
        costLimit: Float,
        equalityRange: Float = 0.01f
    ): NavigationField {
        val field = NavigationField(start)
        val prioQueue = PriorityQueue(compareBy(field::getCost))
        val visited = mutableSetOf<Coordinate>()
        prioQueue.add(start)
        while (prioQueue.isNotEmpty()) {
            val current = prioQueue.remove()
            visited.add(current)
            val baseCost = field.getCost(current)
            if (baseCost > costLimit) return field
            for (step in neighbours(current)) {
                if (step.cost < 0 || step.end in visited) continue
                val newCost = baseCost + step.cost
                val oldCost = field.getCost(step.end)
                when {
                    newCost < oldCost - equalityRange -> {
                        prioQueue.remove(step.end)
                        field.setPredecessor(step.end, step.toStartStep(current).first, newCost)
                        prioQueue.add(step.end)
                    }
                    newCost < oldCost + equalityRange -> {
                        field.addPredecessor(step.end, step.toStartStep(current).first)
                    }
                }
            }
        }
        return field
    }

    inline fun createField(
        start: Coordinate,
        neighbours: NeighboursFunction,
        costLimit: Float = Float.POSITIVE_INFINITY,
        matchPredicate: (Coordinate) -> Boolean,
        stopOnMatch: Boolean = true,
        equalityRange: Float = 0.01f
    ): NavigationField {
        val field = NavigationField(start)
        val prioQueue = PriorityQueue(compareBy(field::getCost))
        val visited = mutableSetOf<Coordinate>()
        val notMatchedLocations = mutableSetOf<Coordinate>()
        var maxCost = costLimit + equalityRange
        prioQueue.add(start)
        while (prioQueue.isNotEmpty()) {
            val current = prioQueue.remove()
            visited.add(current)
            val baseCost = field.getCost(current)
            if (baseCost > maxCost) return field
            for (step in neighbours(current)) {
                val next: Coordinate = step.end
                if (step.cost < 0 || next in visited) continue
                val newCost = baseCost + step.cost
                if (newCost > maxCost) continue
                val oldCost = field.getCost(next)
                val updated: Boolean
                updated = when {
                    newCost < oldCost - equalityRange -> {
                        prioQueue.remove(next)
                        field.setPredecessor(next, step.toStartStep(current).first, newCost)
                        prioQueue.add(next)
                        true
                    }
                    newCost < oldCost + equalityRange -> {
                        field.addPredecessor(next, step.toStartStep(current).first)
                        true
                    }
                    else -> false
                }
                if (updated && next !in notMatchedLocations) {
                    if (!field.isMatched(next)) {
                        if (!matchPredicate(next)) {
                            notMatchedLocations.add(next)
                            continue
                        }
                        field.addMatched(next)
                    }
                    if (stopOnMatch)
                        maxCost = min(maxCost, newCost + equalityRange)
                }
            }
        }
        field.filterMatched { field.getCost(it) < maxCost }
        return field
    }

    fun shortestPaths(
        start: Coordinate,
        neighbours: NeighboursFunction,
        equalityRange: Float = 0.01f,
        stopOnMatch: Boolean = true,
        matchPredicate: (Coordinate) -> Boolean,
    ) = createField(
        start,
        neighbours,
        matchPredicate = matchPredicate,
        stopOnMatch = stopOnMatch,
        equalityRange = equalityRange
    ).findRoutesToMatched(true, equalityRange)

    fun shortestPaths(
        start: Coordinate,
        neighbours: NeighboursFunction,
        target: Coordinate,
        equalityRange: Float = 0.01f,
        stopOnMatch: Boolean = true
    ): List<Route> = createField(
        start,
        neighbours,
        matchPredicate = { it == target },
        stopOnMatch = stopOnMatch,
        equalityRange = equalityRange
    ).findRoutes(target)
}

fun LookupPlanet.getNeighbours(location: Coordinate): List<Route.EndStep> =
    getPaths(location).mapNotNull {
        if (it.blocked) null
        else Route.EndStep(
            it.sourceDirection,
            it.target,
            (it.weight ?: return@mapNotNull null).toFloat()
        )
    }

fun Planet.createNeighboursFunction(): NeighboursFunction = LookupPlanet(this)::getNeighbours

fun Map<Coordinate, Map<Direction, Path>>.getNeighbours(location: Coordinate): List<Route.EndStep> =
    this[location]?.mapNotNull {
        if (it.value.blocked) return@mapNotNull null
        Route.EndStep(it.key, it.value.target, (it.value.weight ?: return@mapNotNull null).toFloat())
    } ?: emptyList()

fun NavigatorState.getNeighbours(location: Coordinate): List<Route.EndStep> = paths.getNeighbours(location)
