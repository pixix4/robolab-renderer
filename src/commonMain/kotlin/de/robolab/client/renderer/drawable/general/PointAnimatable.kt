package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.drawable.base.Animatable
import de.robolab.client.renderer.drawable.edit.IEditCallback
import de.robolab.client.renderer.events.PointerEvent
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.client.renderer.view.base.menu
import de.robolab.client.renderer.view.component.SquareView
import de.robolab.common.planet.PlanetPoint
import de.robolab.common.planet.PlanetDirection
import de.robolab.common.planet.Planet
import kotlin.math.roundToLong

class PointAnimatable(
    reference: PointAnimatableManager.AttributePoint,
    private var planet: Planet,
    private val editCallback: IEditCallback?,
) : Animatable<PointAnimatableManager.AttributePoint>(reference) {

    override val view = SquareView(
        reference.coordinate.point,
        PlottingConstraints.POINT_SIZE,
        PlottingConstraints.LINE_WIDTH * 0.65,
        calcColor(planet),
        !reference.hidden
    )

    override fun onUpdate(obj: PointAnimatableManager.AttributePoint, planet: Planet) {
        super.onUpdate(obj, planet)

        this.planet = planet
        view.setColor(calcColor(planet))
        view.setIsFilled(!obj.hidden)
    }

    private fun calcColor(planet: Planet): ViewColor {
        return when (reference.coordinate.getColor(planet.bluePoint)) {
            PlanetPoint.Color.Red -> ViewColor.POINT_RED
            PlanetPoint.Color.Blue -> ViewColor.POINT_BLUE
            PlanetPoint.Color.Unknown -> ViewColor.GRID_TEXT_COLOR
        }
    }

    init {
        view.focusable = true

        view.registerPointerHint(
            {
                val focusedView = view.document?.focusedStack?.lastOrNull() as? SquareView
                if (focusedView != null) {
                    val exposureCoordinate = PlanetPoint(
                        focusedView.center.left.roundToLong(),
                        focusedView.center.top.roundToLong()
                    )
                    "Toggle target (Exposure: ${exposureCoordinate})"
                } else {
                    "Toggle target"
                }
            },
            PointerEvent.Type.DOWN,
            ctrlKey = true
        ) {
            val focusedView = view.document?.focusedStack?.lastOrNull() as? SquareView
            editCallback != null && focusedView != null
        }
        view.onPointerDown { event ->
            val callback = editCallback ?: return@onPointerDown
            val focusedView = view.document?.focusedStack?.lastOrNull() as? SquareView
            if (event.ctrlKey && focusedView != null) {
                val targetCoordinate = PlanetPoint(
                    view.center.left.roundToLong(),
                    view.center.top.roundToLong()
                )

                val exposureCoordinate = PlanetPoint(
                    focusedView.center.left.roundToLong(),
                    focusedView.center.top.roundToLong()
                )

                callback.toggleTargetExposure(targetCoordinate, exposureCoordinate)

                event.stopPropagation()
            }
        }

        view.onPointerSecondaryAction { event ->
            val callback = editCallback ?: return@onPointerSecondaryAction
            event.stopPropagation()

            val coordinate = this.reference.coordinate

            val pathList = planet.paths.filter { it.connectsWith(coordinate) }
            val pathExposureList = planet.paths.filter { path ->
                path.exposure.any { it.planetPoint == coordinate }
            }
            val targetList = planet.targets.filter { it.point == coordinate || coordinate in it.exposure }
            val pathSelectList = planet.pathSelects.filter { it.point == coordinate }
            val startPoint = planet.startPoint.point
            val isStartPoint = startPoint == coordinate

            view.menu(event, "Point ${coordinate.x}, ${coordinate.y}") {
                if (isStartPoint) {
                    action("Remove start edge") {
                        callback.deleteStartPoint(false)
                    }
                } else {
                    val openDirections = (PlanetDirection.values().toList() - pathList.flatMap {
                        listOf(
                            it.source to it.sourceDirection,
                            it.target to it.targetDirection
                        )
                    }.filter { it.first == coordinate }.map { it.second.opposite() })

                    if (openDirections.isNotEmpty()) {
                        menu("Add start edge") {
                            for (direction in openDirections) {
                                action(direction.name.lowercase()
                                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }) {
                                    callback.setStartPoint(coordinate, direction)
                                }
                            }
                        }
                    }
                }

                menu("Toggle path select") {
                    for (direction in PlanetDirection.values()) {
                        val isChecked = pathSelectList.find { it.direction == direction } != null
                        action(direction.name.lowercase()
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }, isChecked) {
                            callback.togglePathSelect(coordinate, direction)
                        }
                    }
                }

                when (coordinate.getColor(planet.bluePoint)) {
                    PlanetPoint.Color.Red -> {
                        action("Mark as blue") {
                            callback.setBluePoint(coordinate)
                        }
                    }
                    PlanetPoint.Color.Blue -> {
                        action("Mark as red") {
                            callback.setBluePoint(PlanetPoint(coordinate.x + 1, coordinate.y))
                        }
                    }
                    PlanetPoint.Color.Unknown -> {
                        action("Mark as blue") {
                            callback.setBluePoint(coordinate)
                        }
                        action("Mark as red") {
                            callback.setBluePoint(PlanetPoint(coordinate.x + 1, coordinate.y))
                        }
                    }
                }

                menu("Transform") {
                    if (startPoint != coordinate) {
                        action("Translate start point") {
                            callback.translate(
                                PlanetPoint(
                                    coordinate.x - startPoint.x,
                                    coordinate.y - startPoint.y
                                )
                            )
                        }
                    }

                    action("Rotate clockwise") {
                        callback.rotate(Planet.RotateDirection.CLOCKWISE, coordinate)
                    }
                    action("Rotate counter clockwise") {
                        editCallback.rotate(Planet.RotateDirection.COUNTER_CLOCKWISE, coordinate)
                    }
                }

                action("Delete") {
                    var grouping = false
                    for (path in pathExposureList) {
                        for (exposure in path.exposure) {
                            if (exposure.planetPoint == coordinate) {
                                callback.togglePathExposure(path, exposure, grouping)
                                grouping = true
                            }
                        }
                    }
                    for (target in targetList) {
                        for (e in target.exposure) {
                            callback.toggleTargetExposure(target.point, e, grouping)
                            grouping = true
                        }
                    }
                    for (pathSelect in pathSelectList) {
                        callback.togglePathSelect(pathSelect.point, pathSelect.direction, grouping)
                        grouping = true
                    }
                    for (path in pathList) {
                        callback.deletePath(path, grouping)
                        grouping = true
                    }
                    if (isStartPoint) {
                        callback.deleteStartPoint(grouping)
                    }
                }
            }
        }
    }
}
