package de.robolab.renderer.interaction

import de.robolab.drawable.EditControlPointsDrawable
import de.robolab.drawable.EditDrawEndDrawable
import de.robolab.drawable.EditPlanetDrawable
import de.robolab.renderer.data.Point
import de.robolab.renderer.platform.ICanvasListener
import de.robolab.renderer.platform.MouseEvent

class EditPlanetInteraction(
        private val editPlanet: EditPlanetDrawable
) : ICanvasListener {

    var startEnd: EditDrawEndDrawable.PointEnd? = null
    private var hasMovedWhileDown = false
    private var shouldCreatePath = false

    override fun onMouseDown(event: MouseEvent): Boolean {
        hasMovedWhileDown = false

        if (startEnd == null) {
            if (editPlanet.pointer.findObjectUnderPointer<EditControlPointsDrawable.ControlPoint>() != null) return false

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
        return onMouseDrag(event)
    }

    override fun onMouseDrag(event: MouseEvent): Boolean {
        hasMovedWhileDown = true
        shouldCreatePath = true

        return false
    }

    override fun onMouseClick(event: MouseEvent): Boolean {
        if (!hasMovedWhileDown) {
            editPlanet.selectedPath = editPlanet.pointer.findObjectUnderPointer()
        }

        return false
    }
}
