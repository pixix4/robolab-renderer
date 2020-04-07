package de.robolab.renderer.drawable.edit

import de.robolab.model.Coordinate
import de.robolab.model.Direction
import de.robolab.model.Path
import de.robolab.planet.Planet
import de.robolab.renderer.utils.DrawContext
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.drawable.base.selectedElement
import de.robolab.renderer.drawable.planet.EditPlanetDrawable
import de.robolab.renderer.platform.PointerEvent
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

class EditPointDrawable(
        private val editPlanetDrawable: EditPlanetDrawable
) : IDrawable {

    private val colorTransition = DoubleTransition(0.0)
    private val sizeTransition = DoubleTransition(0.0)
    private val alphaTransition = DoubleTransition(0.0)

    private val transitions = listOf(colorTransition, sizeTransition, alphaTransition)

    private var oldPlanetIsEvenBlue: Boolean? = null
    private var planetIsEvenBlue: Boolean? = null

    override fun onUpdate(ms_offset: Double): Boolean {
        var hasChanges = false

        for (animatable in transitions) {
            if (animatable.update(ms_offset)) {
                hasChanges = true
            }
        }

        return hasChanges
    }

    override fun onDraw(context: DrawContext) {
        if (sizeTransition.value == 0.0 || alphaTransition.value == 0.0) return

        val size = Point(PlottingConstraints.POINT_SIZE / 2, PlottingConstraints.POINT_SIZE / 2) * sizeTransition.value

        val redColor = context.theme.redColor
                .interpolate(context.theme.secondaryBackgroundColor, COLOR_OPACITY)
                .a(alphaTransition.value)
        val blueColor = context.theme.blueColor
                .interpolate(context.theme.secondaryBackgroundColor, COLOR_OPACITY)
                .a(alphaTransition.value)
        val greyColor = context.theme.gridTextColor
                .interpolate(context.theme.secondaryBackgroundColor, COLOR_OPACITY)
                .a(alphaTransition.value)

        val selectedPoint = editPlanetDrawable.selectedElement<Coordinate>()

        for (x in floor(context.area.left).toInt()..ceil(context.area.right).toInt()) {
            for (y in floor(context.area.top).toInt()..ceil(context.area.bottom).toInt()) {
                val isThisPointEven = (x + y) % 2 == 0
                val position = Point(x.toDouble(), y.toDouble())

                val oldColor = oldPlanetIsEvenBlue?.let {
                    if (it == isThisPointEven) {
                        blueColor
                    } else {
                        redColor
                    }
                } ?: greyColor

                val newColor = planetIsEvenBlue?.let {
                    if (it == isThisPointEven) {
                        blueColor
                    } else {
                        redColor
                    }
                } ?: greyColor

                if (selectedPoint?.x == x && selectedPoint.y == y) {
                    val selectedSize = size + Point(PlottingConstraints.HOVER_WIDTH, PlottingConstraints.HOVER_WIDTH)
                    context.fillRect(Rectangle.fromEdges(
                            position - selectedSize,
                            position + selectedSize
                    ), context.theme.highlightColor)
                }

                context.fillRect(Rectangle.fromEdges(
                        position - size,
                        position + size
                ), oldColor.interpolate(newColor, colorTransition.value))
            }
        }
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        if (!editPlanetDrawable.editable) return emptyList()

        val point = Point(position.left.roundToInt(), position.top.roundToInt())
        if (position.distance(point) < PlottingConstraints.POINT_SIZE / 2) {
            return listOf(Coordinate(point.left.toInt(), point.top.toInt()))
        }

        return emptyList()
    }

    fun startExitAnimation(onFinish: () -> Unit) {
        sizeTransition.animate(0.0, editPlanetDrawable.animationTime / 2, editPlanetDrawable.animationTime / 2)
        sizeTransition.onFinish.clearListeners()
        sizeTransition.onFinish { onFinish() }

        alphaTransition.animate(0.0, editPlanetDrawable.animationTime / 2, editPlanetDrawable.animationTime / 2)
    }

    fun startEnterAnimation(onFinish: () -> Unit) {
        sizeTransition.animate(1.0, editPlanetDrawable.animationTime / 2, editPlanetDrawable.animationTime / 2)
        sizeTransition.onFinish.clearListeners()
        sizeTransition.onFinish { onFinish() }

        alphaTransition.animate(1.0, editPlanetDrawable.animationTime / 2, editPlanetDrawable.animationTime / 2)
    }

    private var planet = Planet.EMPTY
    fun importPlanet(planet: Planet) {
        this.planet = planet
        oldPlanetIsEvenBlue = planetIsEvenBlue
        planetIsEvenBlue = planet.bluePoint?.let {
            (it.x + it.y) % 2 == 0
        }

        colorTransition.resetValue(0.0)
        colorTransition.animate(1.0, editPlanetDrawable.animationTime / 2, editPlanetDrawable.animationTime / 4)
    }

    companion object {
        private const val COLOR_OPACITY = 0.85
    }

    override fun onPointerUp(event: PointerEvent, position: Point): Boolean {
        if (!editPlanetDrawable.editable || event.hasMoved) return false

        val currentPoint = editPlanetDrawable.pointer?.findObjectUnderPointer<Coordinate>()
        val currentPath = editPlanetDrawable.pointer?.findObjectUnderPointer<Path>()
        val pointSelect = editPlanetDrawable.pointer?.findObjectUnderPointer<EditPathSelectDrawable.PointSelect>()

        val selectedPoint = editPlanetDrawable.selectedElement<Coordinate>()
        if (selectedPoint != null && (event.ctrlKey || event.altKey)) {
            if (currentPoint != null) {
                editPlanetDrawable.editCallback.toggleTargetExposure(currentPoint, selectedPoint)
            } else if (currentPath != null) {
                editPlanetDrawable.editCallback.togglePathExposure(currentPath, selectedPoint)
            }
            return true
        } else {
            if (pointSelect == null) {
                editPlanetDrawable.selectedElements = listOfNotNull(
                        currentPoint,
                        if (editPlanetDrawable.selectedElement<Coordinate>() == null) {
                            currentPath
                        } else {
                            null
                        }
                )
            }
        }

        return false
    }

    override fun onPointerSecondaryAction(event: PointerEvent, position: Point): Boolean {
        if (!editPlanetDrawable.editable) return false

        val coordinate = editPlanetDrawable.pointer?.findObjectUnderPointer<Coordinate>()
        if (coordinate != null) {
            showPointContextMenu(coordinate)
            return true
        }

        val controlPoint = editPlanetDrawable.pointer?.findObjectUnderPointer<EditControlPointsDrawable.ControlPoint>()
        if (controlPoint != null && controlPoint.newPoint == null) {
            showControlPointContextMenu(controlPoint)
        }

        val path = editPlanetDrawable.pointer?.findObjectUnderPointer<Path>()
        if (path != null) {
            showPathContextMenu(path)
        }

        return false
    }

    private fun showPointContextMenu(coordinate: Coordinate) {
        val pathList = planet.pathList.filter { it.source == coordinate || it.target == coordinate }
        val pathExposureList = planet.pathList.filter { coordinate in it.exposure }
        val targetList = planet.targetList.filter { it.target == coordinate || it.exposure == coordinate }
        val pathSelectList = planet.pathSelectList.filter { it.point == coordinate }
        val isStartPoint = planet.startPoint?.point == coordinate

        editPlanetDrawable.menu("Point ${coordinate.x}, ${coordinate.y}") {
            if (isStartPoint) {
                action("Remove start edge") {
                    editPlanetDrawable.editCallback.deleteStartPoint(false)
                }
            } else {
                val openDirections = (Direction.values().toList() - pathList.flatMap { listOf(it.source to it.sourceDirection, it.target to it.targetDirection) }.filter { it.first == coordinate }.map { it.second.opposite() })

                if (openDirections.isNotEmpty()) {
                    menu("Add start edge") {
                        for (direction in openDirections) {
                            action(direction.name.toLowerCase().capitalize()) {
                                editPlanetDrawable.editCallback.setStartPoint(coordinate, direction)
                            }
                        }
                    }
                }
            }

            menu("Toggle path select") {
                for (direction in Direction.values()) {
                    action(direction.name.toLowerCase().capitalize()) {
                        editPlanetDrawable.editCallback.togglePathSelect(coordinate, direction)
                    }
                }
            }

            when (coordinate.getColor(planet.bluePoint)) {
                Coordinate.Color.RED -> {
                    action("Mark as blue") {
                        editPlanetDrawable.editCallback.setBluePoint(coordinate)
                    }
                }
                Coordinate.Color.BLUE -> {
                    action("Mark as red") {
                        editPlanetDrawable.editCallback.setBluePoint(Coordinate(coordinate.x + 1, coordinate.y))
                    }
                }
                Coordinate.Color.UNKNOWN -> {
                    action("Mark as blue") {
                        editPlanetDrawable.editCallback.setBluePoint(coordinate)
                    }
                    action("Mark as red") {
                        editPlanetDrawable.editCallback.setBluePoint(Coordinate(coordinate.x + 1, coordinate.y))
                    }
                }
            }

            action("Delete") {
                var grouping = false
                for (path in pathExposureList) {
                    editPlanetDrawable.editCallback.togglePathExposure(path, coordinate, grouping)
                    grouping = true
                }
                for (target in targetList) {
                    editPlanetDrawable.editCallback.toggleTargetExposure(target.target, target.exposure, grouping)
                    grouping = true
                }
                for (pathSelect in pathSelectList) {
                    editPlanetDrawable.editCallback.togglePathSelect(pathSelect.point, pathSelect.direction, grouping)
                    grouping = true
                }
                for (path in pathList) {
                    editPlanetDrawable.editCallback.deletePath(path, grouping)
                    grouping = true
                }
                if (isStartPoint) {
                    editPlanetDrawable.editCallback.deleteStartPoint(grouping)
                }
            }
        }
    }

    private fun showControlPointContextMenu(controlPoint: EditControlPointsDrawable.ControlPoint) {
        editPlanetDrawable.menu("Control point ${controlPoint.point}") {
            action("Delete") {
                val path = editPlanetDrawable.selectedElement<Path>() ?: return@action
                val (_, indexP) = controlPoint
                val isOneWayPath = path.source == path.target && path.sourceDirection == path.targetDirection

                val allControlPoints = editPlanetDrawable.selectedPathControlPoints ?: return@action
                val dropLast = if (isOneWayPath) 0 else 1
                val controlPoints = allControlPoints.drop(1).dropLast(dropLast).toMutableList()
                val index = indexP - 1

                if (index <= 0 || index >= controlPoints.lastIndex) return@action

                controlPoints.removeAt(index)

                editPlanetDrawable.editCallback.updatePathControlPoints(path, controlPoints, false)
            }
        }
    }

    private fun showPathContextMenu(path: Path) {
        editPlanetDrawable.menu(
                "Path (${path.source.x}, ${path.source.y}, ${path.sourceDirection.name.first()}) -> (${path.target.x}, ${path.target.y}, ${path.targetDirection.name.first()})"
        ) {
            action("Reset control points") {
                editPlanetDrawable.editCallback.updatePathControlPoints(path, emptyList())
            }

            action(if (path.hidden) "Mark path as visible" else "Mark path as hidden") {
                editPlanetDrawable.editCallback.togglePathHiddenState(path)
            }

            if (path.weight != null) {
                action(if (path.weight < 0) "Unblock path" else "Block path") {
                    editPlanetDrawable.editCallback.setPathWeight(path, if (path.weight < 0) 1 else -1)
                }
            }

            if (path.exposure.isNotEmpty()) {
                action("Remove path sender points") {
                    var grouping = false
                    for (exposure in path.exposure) {
                        editPlanetDrawable.editCallback.togglePathExposure(path, exposure, grouping)
                        grouping = true
                    }
                }
            }

            action("Delete") {
                editPlanetDrawable.editCallback.deletePath(path)
            }
        }
    }
}
