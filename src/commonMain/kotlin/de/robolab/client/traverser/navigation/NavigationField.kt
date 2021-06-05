package de.robolab.client.traverser.navigation

import de.robolab.common.planet.PlanetPoint

class NavigationField(
    val start: PlanetPoint
) {
    private val _predecessors: MutableMap<PlanetPoint, Pair<List<Route.StartStep>, Float>> =
        mutableMapOf(start to (emptyList<Route.StartStep>() to 0f))
    val predecessors: Map<PlanetPoint, Pair<List<Route.StartStep>, Float>> = _predecessors
    private val _matchedLocations = mutableSetOf<PlanetPoint>()
    val matchedLocations: Set<PlanetPoint> = _matchedLocations


    fun setPredecessor(location: PlanetPoint, predecessor: Route.StartStep, totalCost: Float) {
        _predecessors[location] = listOf(predecessor) to totalCost
    }

    fun setPredecessor(location: PlanetPoint, predecessor: List<Route.StartStep>, totalCost: Float) {
        _predecessors[location] = predecessor to totalCost
    }

    fun addPredecessor(location: PlanetPoint, predecessor: Route.StartStep) {
        val oldValue =
            _predecessors[location] ?: throw IllegalStateException("Cannot add predecessor to non-existing list")
        _predecessors[location] = oldValue.copy(first = oldValue.first + predecessor)
    }

    fun getCost(location: PlanetPoint): Float = _predecessors[location]?.second ?: Float.POSITIVE_INFINITY

    fun addMatched(location: PlanetPoint): Boolean = _matchedLocations.add(location)
    fun removeMatched(location: PlanetPoint): Boolean = _matchedLocations.remove(location)
    fun filterMatched(predicate: (PlanetPoint) -> Boolean) = _matchedLocations.retainAll(predicate)

    fun isMatched(location: PlanetPoint): Boolean = location in _matchedLocations

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

    fun findRoutes(target: PlanetPoint): List<Route> = buildRoutes(Route.Builder(target))

    fun findRoutesToMatched(onlyClosest: Boolean = false, equalityRange: Float = 0.01f): List<Route> {
        val routes = matchedLocations.flatMap(::findRoutes)
        return if (onlyClosest && routes.isNotEmpty()) {
            val minCost = routes.minOf(Route::cost) + equalityRange
            routes.filter { it.cost <= minCost }
        } else routes
    }
}
