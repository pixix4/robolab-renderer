package de.robolab.renderer.interaction

import de.robolab.drawable.edit.EditControlPointsDrawable
import de.robolab.drawable.edit.EditDrawEndDrawable
import de.robolab.drawable.edit.EditPathSelectDrawable
import de.robolab.drawable.edit.EditPlanetDrawable
import de.robolab.model.Direction
import de.robolab.model.Path
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Point
import de.robolab.renderer.platform.ICanvasListener
import de.robolab.renderer.platform.KeyCode
import de.robolab.renderer.platform.KeyEvent
import de.robolab.renderer.platform.MouseEvent
import kotlin.math.roundToInt

class EditPlanetInteraction(
        private val editPlanet: EditPlanetDrawable
) : ICanvasListener {

    var startEnd: EditDrawEndDrawable.PointEnd? = null
    private var controlPoint: EditControlPointsDrawable.ControlPoint? = null
    private var hasMovedWhileDown = false
    private var shouldCreatePath = false
    private var controlPointsGroupHistory = false

    override fun onMouseDown(event: MouseEvent): Boolean {
        if (!editPlanet.editable) return false

        hasMovedWhileDown = false
        controlPointsGroupHistory = false

        if (startEnd == null) {
            val c = editPlanet.pointer.findObjectUnderPointer<EditControlPointsDrawable.ControlPoint>()
            if (c != null) {
                if (c.newPoint != null) {
                    val allControlPoints = editPlanet.selectedPathControlPoints ?: return false
                    val controlPoints = allControlPoints.drop(1).dropLast(1).toMutableList()

                    controlPoints.add(c.point - 1, c.newPoint)
                    editPlanet.editCallback.updateControlPoints(c.path, controlPoints.map { it.left to it.top }, false)
                }

                controlPoint = c

                return true
            }

            startEnd = editPlanet.pointer.findObjectUnderPointer()
            if (startEnd != null) {
                shouldCreatePath = false
                return true
            }
        }

        return false
    }

    override fun onMouseUp(event: MouseEvent): Boolean {
        if (!editPlanet.editable) return false

        controlPoint = null
        if (startEnd != null && shouldCreatePath) {
            val targetEnd = editPlanet.pointer.findObjectUnderPointer<EditDrawEndDrawable.PointEnd>()

            startEnd?.let { (start, startDirection) ->
                targetEnd?.let { (end, endDirection) ->
                    editPlanet.editCallback.drawPath(
                            start,
                            startDirection,
                            end,
                            endDirection
                    )
                }
            }

            startEnd = null

            return false
        }

        shouldCreatePath = true

        return false
    }

    override fun onMouseMove(event: MouseEvent): Boolean {
        if (!editPlanet.editable) return false

        hasMovedWhileDown = true
        shouldCreatePath = true

        return false
    }

    private fun calculateProjection(referencePoint: Point, direction: Direction): Point {
        val basisVector = direction.toVector()
        val referenceVector = editPlanet.pointer.position - referencePoint

        val distance = referenceVector.dotProduct(basisVector)
        val targetVector = basisVector * distance

        return if (distance > PlottingConstraints.CURVE_FIRST_POINT) {
            referencePoint + targetVector
        } else {
            referencePoint + basisVector * PlottingConstraints.CURVE_FIRST_POINT
        }
    }

    override fun onMouseDrag(event: MouseEvent): Boolean {
        if (!editPlanet.editable) return false

        hasMovedWhileDown = true
        shouldCreatePath = true

        if (controlPoint != null) {
            val path = editPlanet.selectedPath ?: return false

            val (_, indexP) = controlPoint ?: return false
            val allControlPoints = editPlanet.selectedPathControlPoints ?: return false
            val oldControlPoints = allControlPoints.drop(1).dropLast(1)
            val controlPoints = oldControlPoints.toMutableList()
            val index = indexP - 1

            if (index < 0 || index > controlPoints.lastIndex) return false

            controlPoints[index] = when (index) {
                0 -> calculateProjection(
                        allControlPoints.first(),
                        path.sourceDirection
                )
                controlPoints.lastIndex -> calculateProjection(
                        allControlPoints.last(),
                        path.targetDirection
                )
                else -> editPlanet.pointer.position
            }.let {
                Point(
                        (it.left * 10).roundToInt() / 10.0,
                        (it.top * 10).roundToInt() / 10.0
                )
            }

            if (oldControlPoints != controlPoints) {
                editPlanet.editCallback.updateControlPoints(path, controlPoints.map { it.left to it.top }, controlPointsGroupHistory)
                controlPointsGroupHistory = true
            }
        }

        return false
    }

    override fun onMouseClick(event: MouseEvent): Boolean {
        if (!editPlanet.editable) return false

        if (!hasMovedWhileDown) {
            val currentPathSelect = editPlanet.pointer.findObjectUnderPointer<EditPathSelectDrawable.PointSelect>()
            if (currentPathSelect != null) {
                editPlanet.editCallback.togglePathSelect(currentPathSelect.point, currentPathSelect.direction)
                return true
            }
            
            val currentPoint = editPlanet.pointer.findObjectUnderPointer<Pair<Int, Int>>()
            val currentPath = editPlanet.pointer.findObjectUnderPointer<Path>()

            val selectedPoint = editPlanet.selectedPoint
            if (selectedPoint != null && (event.ctrlKey || event.altKey)) {
                if (currentPoint != null) {
                    editPlanet.editCallback.toggleTargetSend(selectedPoint, currentPoint)
                } else if (currentPath != null) {
                    editPlanet.editCallback.togglePathSend(selectedPoint, currentPath)
                }
                return true
            } else {
                editPlanet.selectedPoint = currentPoint
                editPlanet.selectedPath = if (editPlanet.selectedPoint == null) {
                    currentPath
                } else {
                    null
                }
            }
        }

        return false
    }

    override fun onKeyDown(event: KeyEvent): Boolean {
        if (!editPlanet.editable) return false

        when (event.keyCode) {
            KeyCode.DELETE -> {
                val path = editPlanet.selectedPath ?: return false

                if (controlPoint != null) {
                    val (_, indexP) = controlPoint ?: return false
                    val allControlPoints = editPlanet.selectedPathControlPoints ?: return false
                    val controlPoints = allControlPoints.drop(1).dropLast(1).toMutableList()
                    val index = indexP - 1

                    if (index <= 0 || index >= controlPoints.lastIndex) return false

                    controlPoints.removeAt(index)

                    editPlanet.editCallback.updateControlPoints(path, controlPoints.map { it.left to it.top }, false)
                    controlPoint = null
                } else {
                    val cp = editPlanet.pointer.findObjectUnderPointer<EditControlPointsDrawable.ControlPoint>()
                    if (cp == null || cp.newPoint != null) {
                        editPlanet.editCallback.deletePath(path)
                    }
                }
            }
            KeyCode.UNDO -> {
                editPlanet.editCallback.undo()
            }
            KeyCode.AGAIN -> {
                editPlanet.editCallback.redo()
            }
            KeyCode.Z -> if (event.ctrlKey) {
                if (event.shiftKey) {
                    editPlanet.editCallback.redo()
                } else {
                    editPlanet.editCallback.undo()
                }
            }
            KeyCode.ESCAPE -> {
                startEnd = null
            }
            else -> {
                return false
            }
        }
        return true
    }
}
