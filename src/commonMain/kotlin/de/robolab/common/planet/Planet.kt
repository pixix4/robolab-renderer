package de.robolab.common.planet

import de.robolab.common.testing.TestSuite
import kotlin.math.PI

data class Planet(
    val version: PlanetVersion,
    val name: String,
    val startPoint: StartPoint?,
    val bluePoint: Coordinate?,
    val pathList: List<Path>,
    val targetList: List<TargetPoint>,
    val pathSelectList: List<PathSelect>,
    val commentList: List<Comment>,
    val tagMap: Map<String, List<String>>,
    val senderGrouping: Map<Set<Coordinate>, Char>,
    val testSuite: TestSuite,
): IPlanetValue {

    fun importSplines(reference: Planet): Planet {
        var startPoint = this.startPoint
        var pathList = this.pathList

        if (reference.startPoint != null && reference.startPoint.point == startPoint?.point && reference.startPoint.orientation == startPoint.orientation) {
            startPoint = startPoint.copy(controlPoints = reference.startPoint.controlPoints)
        }

        pathList = pathList.map { path ->
            val backgroundPath = reference.pathList.find { it.equalPath(path) } ?: return@map path

            if (backgroundPath.source == path.source && backgroundPath.sourceDirection == path.sourceDirection) {
                path.copy(
                    controlPoints = backgroundPath.controlPoints
                )
            } else {
                path.copy(
                    controlPoints = backgroundPath.controlPoints.reversed()
                )
            }
        }

        return Planet(
            reference.version,
            reference.name,
            startPoint,
            reference.bluePoint,
            pathList,
            targetList,
            pathSelectList,
            commentList,
            tagMap,
            senderGrouping,
            testSuite
        )
    }

    fun importSenderGroups(reference: Planet, visitedPoints: List<Coordinate>): Planet {
        val grouping = reference.senderGrouping.filterKeys { set ->
            set.all { it in visitedPoints }
        }.toMutableMap()

        grouping += senderGrouping.filter { (key, value) ->
            key !in grouping && value !in grouping.values
        }

        return Planet(
            version,
            name,
            startPoint,
            bluePoint,
            pathList,
            targetList,
            pathSelectList,
            commentList,
            tagMap,
            grouping,
            testSuite
        ).generateMissingSenderGroupings()
    }

    fun translate(delta: Coordinate) = Planet(
        version,
        name,
        startPoint?.translate(delta),
        bluePoint?.translate(delta),
        pathList.map { it.translate(delta) },
        targetList.map { it.translate(delta) },
        pathSelectList.map { it.translate(delta) },
        commentList.map { it.translate(delta) },
        tagMap,
        senderGrouping.map { (key, value) ->
            key.map { it.translate(delta) }.toSet() to value
        }.toMap(),
        testSuite.translate(delta),
    )

    fun rotate(direction: RotateDirection, origin: Coordinate = startPoint?.point ?: Coordinate(0, 0)) = Planet(
        version,
        name,
        startPoint?.rotate(direction, origin),
        bluePoint?.rotate(direction, origin),
        pathList.map { it.rotate(direction, origin) },
        targetList.map { it.rotate(direction, origin) },
        pathSelectList.map { it.rotate(direction, origin) },
        commentList.map { it.rotate(direction, origin) },
        tagMap,
        senderGrouping.map { (key, value) ->
            key.map { it.rotate(direction, origin) }.toSet() to value
        }.toMap(),
        testSuite.rotate(direction, origin),
    )

    private fun getDrawableSenderGrouping(): List<Set<Coordinate>> {
        return (targetList.map {
            targetList.filter { i -> it.target == i.target }.map { it.exposure }.toSet()
        } + pathList.map { it.exposure })
            .filterNot { it.isEmpty() }
            .distinct()
    }

    fun generateMissingSenderGroupings(): Planet {
        val missing = getDrawableSenderGrouping() - senderGrouping.keys

        val groupings = senderGrouping.toMutableMap()

        var lastChar = 'A'
        for (set in missing) {
            while (lastChar in groupings.values) {
                lastChar += 1
            }
            groupings += set to lastChar
            lastChar += 1
        }

        return copy(
            senderGrouping = groupings
        )
    }

    fun getDefaultSenderGroupings(): Map<Set<Coordinate>, Char> {
        val missing = getDrawableSenderGrouping()

        val groupings = mutableMapOf<Set<Coordinate>, Char>()

        var lastChar = 'A'
        for (set in missing) {
            while (lastChar in groupings.values) {
                lastChar += 1
            }
            groupings += set to lastChar
            lastChar += 1
        }

        return groupings
    }

    fun scaleWeights(factor: Double = 1.0, offset: Int = 0): Planet {
        return copy(
            pathList = pathList.map { path ->
                path.scaleWeights(factor, offset)
            }
        )
    }

    fun getPointList(): List<Coordinate> {
        return (pathList.flatMap { listOf(it.source, it.target) + it.exposure } +
                targetList.flatMap { listOf(it.exposure, it.target) } +
                pathSelectList.map { it.point } +
                listOfNotNull(startPoint?.point)
                ).distinct()
    }

    enum class RotateDirection(val angle: Double) {
        CLOCKWISE(3 * PI / 2), COUNTER_CLOCKWISE(PI / 2)
    }

    companion object {
        val EMPTY = Planet(
            PlanetVersion.CURRENT,
            "",
            null,
            null,
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            emptyMap(),
            emptyMap(),
            TestSuite.EMPTY
        )
    }
}
