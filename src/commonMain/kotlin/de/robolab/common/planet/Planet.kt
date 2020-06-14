package de.robolab.common.planet

import kotlin.math.PI

data class Planet(
    val version: PlanetVersion,
    val name: String,
    val startPoint: StartPoint?,
    val bluePoint: Coordinate?,
    val pathList: List<Path>,
    val targetList: List<TargetPoint>,
    val pathSelectList: List<PathSelect>,
    val commentList: List<Comment>
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
            commentList
        )
    }

    fun translate(delta: Coordinate) = Planet(
        version,
        name,
        startPoint?.translate(delta),
        bluePoint?.translate(delta),
        pathList.map { it.translate(delta) },
        targetList.map { it.translate(delta) },
        pathSelectList.map { it.translate(delta) },
        commentList.map { it.translate(delta) }
    )

    fun rotate(direction: RotateDirection, origin: Coordinate = startPoint?.point ?: Coordinate(0, 0)) = Planet(
        version,
        name,
        startPoint?.rotate(direction, origin),
        bluePoint?.rotate(direction, origin),
        pathList.map { it.rotate(direction, origin) },
        targetList.map { it.rotate(direction, origin) },
        pathSelectList.map { it.rotate(direction, origin) },
        commentList.map { it.rotate(direction, origin) }
    )

    fun scaleWeights(factor: Double = 1.0, offset: Int = 0): Planet {
        return copy(
            pathList = pathList.map { path ->
                path.scaleWeights(factor, offset)
            }
        )
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
            emptyList()
        )
    }
}
