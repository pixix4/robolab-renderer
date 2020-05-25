package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.Animatable
import de.robolab.client.renderer.drawable.edit.CreatePathManager
import de.robolab.client.renderer.drawable.edit.IEditCallback
import de.robolab.client.renderer.drawable.utils.toPoint
import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.client.renderer.view.base.extraPut
import de.robolab.client.renderer.view.base.menu
import de.robolab.client.renderer.view.component.SquareView
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Direction
import de.robolab.common.planet.Planet
import de.westermann.kobserve.base.ObservableValue
import kotlin.math.roundToInt

class PointAnimatable(
    reference: PointAnimatableManager.AttributePoint,
    private var planet: Planet,
    private val editProperty: ObservableValue<IEditCallback?>,
    private val createPath: CreatePathManager?
) : Animatable<PointAnimatableManager.AttributePoint>(reference) {

    override val view = SquareView(
            reference.coordinate.toPoint(),
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
            Coordinate.Color.RED -> ViewColor.POINT_RED
            Coordinate.Color.BLUE -> ViewColor.POINT_BLUE
            Coordinate.Color.UNKNOWN -> ViewColor.GRID_TEXT_COLOR
        }
    }


    init {
        view.focusable = editProperty.value != null
        editProperty.onChange {
            view.focusable = editProperty.value != null
        }

        view.onPointerDown { event ->
            val callback = editProperty.value ?: return@onPointerDown
            val focusedView = view.document?.focusedStack?.lastOrNull() as? SquareView
            if (event.ctrlKey && focusedView != null) {
                val targetCoordinate = Coordinate(
                        view.center.left.roundToInt(),
                        view.center.top.roundToInt()
                )

                val exposureCoordinate = Coordinate(
                        focusedView.center.left.roundToInt(),
                        focusedView.center.top.roundToInt()
                )

                callback.toggleTargetExposure(targetCoordinate, exposureCoordinate)

                event.stopPropagation()
            }
        }

        view.onPointerSecondaryAction {event ->
            val callback = editProperty.value ?: return@onPointerSecondaryAction
            event.stopPropagation()
            
            val coordinate = this.reference.coordinate
            
            val pathList = planet.pathList.filter { it.connectsWith(coordinate) }
            val pathExposureList = planet.pathList.filter { coordinate in it.exposure }
            val targetList = planet.targetList.filter { it.target == coordinate || it.exposure == coordinate }
            val pathSelectList = planet.pathSelectList.filter { it.point == coordinate }
            val isStartPoint = planet.startPoint?.point == coordinate

            view.menu(event, "Point ${coordinate.x}, ${coordinate.y}") {
                if (isStartPoint) {
                    action("Remove start edge") {
                        callback.deleteStartPoint(false)
                    }
                } else {
                    val openDirections = (Direction.values().toList() - pathList.flatMap { listOf(it.source to it.sourceDirection, it.target to it.targetDirection) }.filter { it.first == coordinate }.map { it.second.opposite() })

                    if (openDirections.isNotEmpty()) {
                        menu("Add start edge") {
                            for (direction in openDirections) {
                                action(direction.name.toLowerCase().capitalize()) {
                                    callback.setStartPoint(coordinate, direction)
                                }
                            }
                        }
                    }
                }

                menu("Toggle path select") {
                    for (direction in Direction.values()) {
                        action(direction.name.toLowerCase().capitalize()) {
                            callback.togglePathSelect(coordinate, direction)
                        }
                    }
                }

                when (coordinate.getColor(planet.bluePoint)) {
                    Coordinate.Color.RED -> {
                        action("Mark as blue") {
                            callback.setBluePoint(coordinate)
                        }
                    }
                    Coordinate.Color.BLUE -> {
                        action("Mark as red") {
                            callback.setBluePoint(Coordinate(coordinate.x + 1, coordinate.y))
                        }
                    }
                    Coordinate.Color.UNKNOWN -> {
                        action("Mark as blue") {
                            callback.setBluePoint(coordinate)
                        }
                        action("Mark as red") {
                            callback.setBluePoint(Coordinate(coordinate.x + 1, coordinate.y))
                        }
                    }
                }

                action("Delete") {
                    var grouping = false
                    for (path in pathExposureList) {
                        callback.togglePathExposure(path, coordinate, grouping)
                        grouping = true
                    }
                    for (target in targetList) {
                        callback.toggleTargetExposure(target.target, target.exposure, grouping)
                        grouping = true
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
        
        for (direction in Direction.values()) {
            setupPointEnd(reference.coordinate, direction, editProperty, createPath)
        }
    }

    companion object {
        fun setupPointEnd(
            coordinate: Coordinate,
            direction: Direction,
            editProperty: ObservableValue<IEditCallback?>,
            createPath: CreatePathManager?
        ): SquareView {
            val squareView = SquareView(
                    coordinate.toPoint() + direction.toVector() * PlottingConstraints.POINT_SIZE,
                    PlottingConstraints.POINT_SIZE,
                    PlottingConstraints.LINE_WIDTH * 0.65,
                    ViewColor.TRANSPARENT
            )
            squareView.animationTime = 0.0
            squareView.extraPut(coordinate)
            squareView.extraPut(direction)

            squareView.onPointerDown { event ->
                val callback = editProperty.value ?: return@onPointerDown

                if (event.altKey) {
                    callback.togglePathSelect(coordinate, direction)
                } else createPath?.startPath(coordinate, direction, event.ctrlKey)

                event.stopPropagation()
            }

            return squareView
        }
    }
}