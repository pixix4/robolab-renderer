package de.robolab.client.traverser

import de.robolab.common.planet.*
import de.robolab.common.utils.intersect
import kotlin.random.Random

fun Random.nextHexString(length: Int = 8): String =
    CharArray(length) { this.nextBits(4).toString(16).first() }.concatToString()

fun ITraverserState<*>.createRenderState(
    original: Planet? = null,
    name: String = "${original?.name ?: "TrailPlanet"}-${Random.nextHexString()}"
): TraverserRenderState = getTrail().createRenderState(original, name)

interface ITraverserTrail {
    val summary: String
        get() = path.map(Pair<Coordinate, Direction?>::second).joinToString {
            when (it) {
                Direction.EAST -> "E"
                Direction.NORTH -> "N"
                Direction.WEST -> "W"
                Direction.SOUTH -> "S"
                null -> "#"
            }
        }
    val locations: List<Coordinate>
        get() = path.map(Pair<Coordinate, Direction?>::first)
    val directions: List<Direction>
        get() = path.mapNotNull(Pair<Coordinate, Direction?>::second)
    val start: Coordinate
        get() = path.first().first
    val end: Coordinate
        get() = path.last().first
    val path: List<Pair<Coordinate, Direction?>>
    val result: ITraverserState.Status
    val resultInfo: Any?
    val mothershipState: IMothershipState
    val navigatorState: INavigatorState
    fun createRenderState(
        original: Planet? = null,
        name: String = "${original?.name ?: "TrailPlanet"}-${Random.nextHexString()}"
    ): TraverserRenderState =
        TraverserRenderState(
            Planet(
                original?.version ?: PlanetVersion.CURRENT,
                name,
                original?.startPoint
                    ?: (path.firstOrNull()?.first)?.let { StartPoint(it, Direction.NORTH, emptyList()) },
                original?.bluePoint,
                (original?.pathList?.intersect(mothershipState.sentPaths, Path::equalPath)
                    ?: mothershipState.sentPaths).toList(),
                (original?.targetList?.intersect(mothershipState.sentTargets)
                    ?: mothershipState.sentTargets).toList(),
                (original?.pathSelectList?.intersect(mothershipState.sentPathSelects)
                    ?: mothershipState.sentPathSelects).toList(),
                emptyList(),
                emptyMap(),
                emptyMap()
            ), mothershipState.toDrawableRobot(), path.mapNotNull {
                it.first to (it.second ?: return@mapNotNull null)
            },
            mothershipState,
            navigatorState,
            this
        )
}

data class TraverserTrail(
    override val path: List<Pair<Coordinate, Direction?>>,
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