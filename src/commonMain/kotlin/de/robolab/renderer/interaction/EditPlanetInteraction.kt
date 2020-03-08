package de.robolab.renderer.interaction

import de.robolab.drawable.EditDrawEndDrawable
import de.robolab.drawable.EditPlanetDrawable
import de.robolab.renderer.platform.ICanvasListener
import de.robolab.renderer.platform.MouseEvent

class EditPlanetInteraction(
        private val editPlanet: EditPlanetDrawable
) : ICanvasListener {

    var startEnd: EditDrawEndDrawable.PointEnd? = null
    private var hasMoved = false

    override fun onMouseDown(event: MouseEvent): Boolean {
        if (startEnd == null) {
            startEnd = editPlanet.pointer.findObjectUnderPointer()
            if (startEnd != null) {
                hasMoved = false

                return true
            }
        }

        return false
    }

    override fun onMouseUp(event: MouseEvent): Boolean {
        if (startEnd != null && hasMoved) {
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
            hasMoved = false
            return true
        }

        if (!hasMoved) {
            hasMoved = true
        }

        return false
    }

    override fun onMouseMove(event: MouseEvent): Boolean {
        return onMouseDrag(event)
    }

    override fun onMouseDrag(event: MouseEvent): Boolean {
        hasMoved = true

        return false
    }

    override fun onMouseClick(event: MouseEvent): Boolean {
        return false
    }
}
