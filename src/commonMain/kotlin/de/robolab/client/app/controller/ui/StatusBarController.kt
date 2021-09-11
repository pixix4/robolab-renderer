package de.robolab.client.app.controller.ui

import de.robolab.client.app.controller.ConnectionController
import de.robolab.client.app.controller.ProgressController
import de.robolab.client.renderer.drawable.utils.normalizeRadiant
import de.robolab.client.renderer.drawable.utils.radiantToDegree
import de.robolab.common.planet.PlanetCoordinate
import de.robolab.common.planet.PlanetPath
import de.robolab.common.utils.Vector
import de.robolab.common.utils.toFixed
import de.westermann.kobserve.property.join
import kotlin.math.roundToInt

class StatusBarController(
    private val connectionController: ConnectionController,
    private val contentController: ContentController,
    private val progressController: ProgressController,
    private val uiController: UiController,
) {
    val connectionList = connectionController.connectionIndicatorList

    val contentList =
        contentController.plotterWindowProperty.join(contentController.pointerProperty) { window, pointer ->
            val list = mutableListOf<String>()

            if (pointer != null) {
                list += "Pointer: ${format(pointer.roundedPosition)}"
            }

            val rotation = window.transformation.rotation
            val degree = (360.0 - rotation.normalizeRadiant().radiantToDegree()).roundToInt()
            if (degree in 1..359) {
                list += "Rotation: $degree°"
            }

            if (window.transformation.flipView) {
                list += "Flipped"
            }

            list.filter { it.isNotBlank() }
        }

    val progressList = progressController.progressList

    private fun format(obj: Any): String = when (obj) {
        is PlanetPath -> "Path(${obj.sourceX},${obj.sourceY},${obj.sourceDirection.name.first()} -> ${obj.targetX},${obj.targetY},${obj.targetDirection.name.first()})"
        is PlanetCoordinate -> "Coordinate(${obj.x},${obj.y})"
        is Vector -> "${obj.left.toFixed(2)},${obj.top.toFixed(2)}"
        else -> obj.toString()
    }

    val fullscreenProperty = uiController.fullscreenProperty

    fun openTerminal() {
        uiController.terminalEnabledProperty.value = true
    }
}
