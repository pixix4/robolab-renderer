package de.robolab.client.traverser.navigation

import de.robolab.common.planet.Coordinate

class NavigationField(
    val start: Coordinate
) {
    private val _predecessors: MutableMap<Coordinate, Pair<List<Route.StartStep>, Float>> =
        mutableMapOf(start to (emptyList<Route.StartStep>() to 0f))
    val predecessors: Map<Coordinate, Pair<List<Route.StartStep>, Float>> = _predecessors
    private val _matchedLocations = mutableSetOf<Coordinate>()
    val matchedLocations: Set<Coordinate> = _matchedLocations


    fun setPredecessor(location: Coordinate, predecessor: Route.StartStep, totalCost: Float) {
        _predecessors[location] = listOf(predecessor) to totalCost
    }

    fun setPredecessor(location: Coordinate, predecessor: List<Route.StartStep>, totalCost: Float) {
        _predecessors[location] = predecessor to totalCost
    }

    fun addPredecessor(location: Coordinate, predecessor: Route.StartStep) {
        val oldValue =
            _predecessors[location] ?: throw IllegalStateException("Cannot add predecessor to non-existing list")
        _predecessors[location] = oldValue.copy(first = oldValue.first + predecessor)
    }

    fun getCost(location: Coordinate): Float = _predecessors[location]?.second ?: Float.POSITIVE_INFINITY

    fun addMatched(location: Coordinate): Boolean = _matchedLocations.add(location)
    fun removeMatched(location: Coordinate): Boolean = _matchedLocations.remove(location)
    fun filterMatched(predicate: (Coordinate) -> Boolean) = _matchedLocations.retainAll(predicate)

    fun isMatched(location: Coordinate): Boolean = location in _matchedLocations

    private fun buildRoutes(builder: Route.Builder): List<Route> {
        val predecessors = this._predecessors[builder.start]?.first ?: return emptyList()
        if (predecessors.isEmpty()) return listOf(builder.build())
        return if (predecessors.size == 1) {
            builder.prepend(predecessors.single())
            buildRoutes(builder)
        } else predecessors.flatMap {
            val clone = builder.clone()
            clone.prepend(it)
            buildRoutes(clone)
        }
    }

    fun findRoutes(target: Coordinate): List<Route> = buildRoutes(Route.Builder(target))

    fun findRoutesToMatched(onlyClosest: Boolean = false, equalityRange: Float = 0.01f): List<Route> {
        val routes = matchedLocations.flatMap(::findRoutes)
        return if (onlyClosest) {
            val minCost = routes.minOf(Route::cost) + equalityRange
            routes.filter { it.cost <= minCost }
        } else routes
    }
}