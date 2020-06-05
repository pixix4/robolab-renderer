package de.robolab.common.planet

import kotlin.math.PI
import kotlin.math.roundToInt

data class Planet(
    val name: String,
    val startPoint: StartPoint?,
    val bluePoint: Coordinate?,
    val pathList: List<Path>,
    val targetList: List<TargetPoint>,
    val pathSelectList: List<PathSelect>,
    val commentList: List<Comment>
) {

    fun importSplines(reference: Planet): Planet {
        var tmp = this

        tmp = tmp.copy(
            name = reference.name,
            bluePoint = reference.bluePoint
        )

        if (reference.startPoint != null && reference.startPoint.point == tmp.startPoint?.point && reference.startPoint.orientation == tmp.startPoint?.orientation) {
            tmp = tmp.copy(
                startPoint = tmp.startPoint?.copy(controlPoints = reference.startPoint.controlPoints)
            )
        }

        tmp = tmp.copy(
            pathList = tmp.pathList.map { path ->
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
        )

        return tmp
    }

    fun translate(delta: Coordinate) = Planet(
        name,
        startPoint?.translate(delta),
        bluePoint?.translate(delta),
        pathList.map { it.translate(delta) },
        targetList.map { it.translate(delta) },
        pathSelectList.map { it.translate(delta) },
        commentList.map { it.translate(delta) }
    )

    fun rotate(direction: RotateDirection, origin: Coordinate = startPoint?.point ?: Coordinate(0, 0)) = Planet(
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
                path.copy(
                    weight = path.weight?.let { weight ->
                        if (weight > 0) (weight * factor).roundToInt() + offset else weight
                    }
                )
            }
        )
    }

    enum class RotateDirection(val angle: Double) {
        CLOCKWISE(3 * PI / 2), COUNTER_CLOCKWISE(PI / 2)
    }

    companion object {
        val EMPTY = Planet(
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
