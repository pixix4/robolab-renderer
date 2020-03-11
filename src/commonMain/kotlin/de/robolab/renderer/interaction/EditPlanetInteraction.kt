package de.robolab.renderer.interaction

import de.robolab.drawable.EditControlPointsDrawable
import de.robolab.drawable.EditDrawEndDrawable
import de.robolab.drawable.EditPlanetDrawable
import de.robolab.model.Direction
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Point
import de.robolab.renderer.platform.ICanvasListener
import de.robolab.renderer.platform.MouseEvent
import kotlin.math.roundToInt

class EditPlanetInteraction(
        private val editPlanet: EditPlanetDrawable
) : ICanvasListener {

    var startEnd: EditDrawEndDrawable.PointEnd? = null
    var controlPoint: EditControlPointsDrawable.ControlPoint? = null
    private var hasMovedWhileDown = false
    private var shouldCreatePath = false

    override fun onMouseDown(event: MouseEvent): Boolean {
        hasMovedWhileDown = false

        if (startEnd == null) {
            controlPoint = editPlanet.pointer.findObjectUnderPointer()
            if (controlPoint != null) {
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
        if (startEnd != null && shouldCreatePath) {
            val targetEnd = editPlanet.pointer.findObjectUnderPointer<EditDrawEndDrawable.PointEnd>()

            startEnd?.let { (start, startDirection) ->
                targetEnd?.let { (end, endDirection) ->
                    editPlanet.editCallback.onDrawPath(
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
                editPlanet.editCallback.onUpdateControlPoints(path, controlPoints.map { it.left to it.top })
            }
        }

        return false
    }

    override fun onMouseClick(event: MouseEvent): Boolean {
        if (!hasMovedWhileDown) {
            editPlanet.selectedPath = editPlanet.pointer.findObjectUnderPointer()
        }

        return false
    }
}
