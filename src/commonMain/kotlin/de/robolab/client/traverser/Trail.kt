package de.robolab.client.traverser

import de.robolab.common.planet.*
import de.robolab.common.planet.utils.PlanetVersion
import de.robolab.common.utils.intersect
import kotlin.random.Random

fun Random.nextHexString(length: Int = 8): String =
    CharArray(length) { this.nextBits(4).toString(16).first() }.concatToString()

fun ITraverserState<*>.createRenderState(
    original: Planet? = null,
    isBackward: Boolean = false,
    name: String = "${original?.name ?: "TrailPlanet"}-${Random.nextHexString()}"
): TraverserRenderState = getTrail().createRenderState(original, isBackward, name)

interface ITraverserTrail {
    val summary: String
        get() = path.map(Pair<PlanetPoint, PlanetDirection?>::second).joinToString {
            when (it) {
                PlanetDirection.East -> "E"
                PlanetDirection.North -> "N"
                PlanetDirection.West -> "W"
                PlanetDirection.South -> "S"
                null -> "#"
            }
        }
    val locations: List<PlanetPoint>
        get() = path.map(Pair<PlanetPoint, PlanetDirection?>::first)
    val directions: List<PlanetDirection>
        get() = path.mapNotNull(Pair<PlanetPoint, PlanetDirection?>::second)
    val start: PlanetPoint
        get() = path.first().first
    val end: PlanetPoint
        get() = path.last().first
    val path: List<Pair<PlanetPoint, PlanetDirection?>>
    val result: ITraverserState.Status
    val resultInfo: Any?
    val mothershipState: IMothershipState
    val navigatorState: INavigatorState

    fun createRenderState(
        original: Planet? = null,
        isBackward: Boolean = false,
        name: String = "${original?.name ?: "TrailPlanet"}-${Random.nextHexString()}"
    ): TraverserRenderState {
        val planet = Planet.EMPTY.copy(
            version = original?.version ?: PlanetVersion.CURRENT,
            name = name,
            startPoint = original?.startPoint
                ?: (path.firstOrNull()?.first)?.let { PlanetStartPoint(it.x, it.y, PlanetDirection.North) } ?: PlanetStartPoint(0L, 0L, PlanetDirection.North),
            bluePoint = original?.bluePoint,
            paths=(original?.paths?.intersect(mothershipState.sentPaths, PlanetPath::equalPath)
                ?: mothershipState.sentPaths).toList(),
            targets = (original?.targets?.intersect(mothershipState.sentTargets)
                ?: mothershipState.sentTargets).toList(),
            pathSelects = (original?.pathSelects?.intersect(mothershipState.sentPathSelects)
                ?: mothershipState.sentPathSelects).toList(),
        )

        return TraverserRenderState(
            planet,
            mothershipState.toDrawableRobot(isBackward),
            path.mapNotNull {
                it.first to (it.second ?: return@mapNotNull null)
            },
            mothershipState,
            navigatorState,
            this
        )
    }
}

data class TraverserTrail(
    override val path: List<Pair<PlanetPoint, PlanetDirection?>>,
    override val mothershipState: IMothershipState,
    override val navigatorState: INavigatorState,
    override val result: ITraverserState.Status,
    override val resultInfo: Any? = null
) : ITraverserTrail {
    override fun toString(): String =
        "(${start.x}, ${start.y}) -> [$summary] -> (${end.x}, ${end.y}): $result" +
                if (resultInfo != null) " ($resultInfo)"
                else ""
}
