package de.robolab.planet

import de.robolab.model.*

data class Planet(
        val name: String,
        val startPoint: StartPoint?,
        val bluePoint: Coordinate?,
        val pathList: List<Path>,
        val targetList: List<TargetPoint>,
        val pathSelectList: List<PathSelect>
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

                    path.copy(
                            controlPoints = backgroundPath.controlPoints
                    )
                }
        )

        return tmp
    }
    
    companion object {
        val EMPTY = Planet(
                "",
                null,
                null,
                emptyList(),
                emptyList(),
                emptyList()
        )
    }
}
