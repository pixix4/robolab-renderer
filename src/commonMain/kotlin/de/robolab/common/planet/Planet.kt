package de.robolab.common.planet

import de.robolab.common.planet.test.PlanetTestSuite
import de.robolab.common.planet.utils.IPlanetValue
import de.robolab.common.planet.utils.rotate
import de.robolab.common.planet.utils.scaleWeights
import de.robolab.common.planet.utils.translate
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.PI


@Serializable
data class Planet(
    val bluePoint: PlanetPoint? = null,
    val comments: List<PlanetComment> = emptyList(),
    val name: String,
    val paths: List<PlanetPath>,
    val pathSelects: List<PlanetPathSelect>,
    val senderGroupings: List<PlanetSenderGrouping> = emptyList(),
    val startPoint: PlanetStartPoint,
    val tags: Map<String, List<String>> = emptyMap(),
    val targets: List<PlanetTarget>,
    val testSuite: PlanetTestSuite? = null,
    val version: Long
) : IPlanetValue<Planet> {

    @Transient
    val senderGroupingsMap = senderGroupings.associate { it.sender to it.name }

    fun importSplines(reference: Planet): Planet {
        return copy(
            name = reference.name,
            version = reference.version,
            bluePoint = reference.bluePoint,
            startPoint = reference.startPoint,
            paths = paths.map {
                for (p in reference.paths) {
                    if (it.equalPath(p, false)) {
                        return@map it.copy(spline = p.spline)
                    } else if (it.equalPath(p.reversed(), false)) {
                        return@map it.copy(spline = p.spline?.reversed())
                    }
                }

                it
            }
        )
    }

    fun importSenderGroups(reference: Planet, visitedPoints: List<PlanetPoint>): Planet {
        return copy(
            senderGroupings = reference.senderGroupings
        ).generateMissingSenderGroupings()
    }

    private fun getDrawableSenderGrouping(): List<Set<PlanetPoint>> {
        val targetExposureGrouping = targets.map {
            it.exposure
        }
        val pathExposureGrouping = paths.flatMap { path ->
            path.exposure
                .groupBy { it.changes }
                .values.map { group ->
                    group.map { it.planetPoint }.toSet()
                }
        }

        return (targetExposureGrouping + pathExposureGrouping)
            .filterNot { it.isEmpty() }
            .distinct()
    }

    fun generateMissingSenderGroupings(): Planet {
        val missing = getDrawableSenderGrouping() - senderGroupings.map { it.sender }

        val groupings = senderGroupings.toMutableList()

        var lastChar = 'A'
        for (set in missing) {
            while (groupings.any { it.name == lastChar.toString() }) {
                lastChar += 1
            }
            groupings += PlanetSenderGrouping(lastChar.toString(), set)
            lastChar += 1
        }

        return copy(
            senderGroupings = groupings
        )
    }

    fun getDefaultSenderGroupings(): List<PlanetSenderGrouping> {
        val missing = getDrawableSenderGrouping()

        val groupings = mutableListOf<PlanetSenderGrouping>()

        var lastChar = 'A'
        for (set in missing) {
            while (groupings.any { it.name == lastChar.toString() }) {
                lastChar += 1
            }
            groupings += PlanetSenderGrouping(lastChar.toString(), set)
            lastChar += 1
        }

        return groupings
    }

    fun getPointList(): List<PlanetPoint> {
        return (
                paths.flatMap {
                    listOf(
                        PlanetPoint(it.sourceX, it.sourceY),
                        PlanetPoint(it.targetX, it.targetY)
                    ) + it.exposure.map { it.planetPoint }
                } + targets.flatMap {
                    listOf(
                        PlanetPoint(it.x, it.y)
                    ) + it.exposure
                } + pathSelects.map { PlanetPoint(it.x, it.y) } + PlanetPoint(startPoint.x, startPoint.y)
                ).distinct()
    }

    override fun translate(delta: PlanetPoint) = copy(
        bluePoint = bluePoint.translate(delta),
        comments = comments.translate(delta),
        paths = paths.translate(delta),
        pathSelects = pathSelects.translate(delta),
        senderGroupings = senderGroupings.translate(delta),
        startPoint = startPoint.translate(delta),
        targets = targets.translate(delta),
        testSuite = testSuite.translate(delta),
    )

    override fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint) = copy(
        bluePoint = bluePoint.rotate(direction, origin),
        comments = comments.rotate(direction, origin),
        paths = paths.rotate(direction, origin),
        pathSelects = pathSelects.rotate(direction, origin),
        senderGroupings = senderGroupings.rotate(direction, origin),
        startPoint = startPoint.rotate(direction, origin),
        targets = targets.rotate(direction, origin),
        testSuite = testSuite.rotate(direction, origin),
    )

    override fun scaleWeights(factor: Double, offset: Long) = copy(
        paths = paths.scaleWeights(factor, offset)
    )

    enum class RotateDirection(val angle: Double) {
        CLOCKWISE(3 * PI / 2), COUNTER_CLOCKWISE(PI / 2)
    }

    companion object {
        val EMPTY = Planet(
            bluePoint = null,
            comments = emptyList(),
            name = "",
            paths = emptyList(),
            pathSelects = emptyList(),
            senderGroupings = emptyList(),
            startPoint = PlanetStartPoint(
                x = 0L,
                y = 0L,
                orientation = PlanetDirection.North,
                spline = null,
            ),
            tags = emptyMap(),
            targets = emptyList(),
            testSuite = null,
            version = 0L,
        )
    }
}
