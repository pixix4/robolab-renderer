package de.robolab.renderer.drawable.base

import de.robolab.renderer.IPlotter
import de.robolab.renderer.data.Dimension
import de.robolab.renderer.utils.DrawContext
import de.robolab.renderer.data.Point
import de.robolab.renderer.platform.KeyEvent
import de.robolab.renderer.platform.PointerEvent

open class GroupDrawable(vararg drawables: IDrawable) : IDrawable {

    open val drawableList: List<IDrawable> = drawables.toList()

    override fun onUpdate(ms_offset: Double): Boolean {
        var hasChanges = false

        for (drawable in drawableList) {
            if (drawable.onUpdate(ms_offset)) {
                hasChanges = true
            }
        }

        return hasChanges
    }

    override fun onDraw(context: DrawContext) {
        for (drawable in drawableList) {
            drawable.onDraw(context)
        }
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        return drawableList.asReversed().flatMap { it.getObjectsAtPosition(context, position) }
    }

    override fun onPointerDown(event: PointerEvent, position: Point): Boolean {
        for (drawable in drawableList.asReversed()) {
            if (drawable.onPointerDown(event, position)) {
                return true
            }
        }

        return false
    }

    override fun onPointerUp(event: PointerEvent, position: Point): Boolean {
        for (drawable in drawableList.asReversed()) {
            if (drawable.onPointerUp(event, position)) {
                return true
            }
        }

        return false
    }

    override fun onPointerMove(event: PointerEvent, position: Point): Boolean {
        for (drawable in drawableList.asReversed()) {
            if (drawable.onPointerMove(event, position)) {
                return true
            }
        }

        return false
    }

    override fun onPointerDrag(event: PointerEvent, position: Point): Boolean {
        for (drawable in drawableList.asReversed()) {
            if (drawable.onPointerDrag(event, position)) {
                return true
            }
        }

        return false
    }

    override fun onPointerSecondaryAction(event: PointerEvent, position: Point): Boolean {
        for (drawable in drawableList.asReversed()) {
            if (drawable.onPointerSecondaryAction(event, position)) {
                return true
            }
        }

        return false
    }

    override fun onKeyPress(event: KeyEvent): Boolean {
        for (drawable in drawableList.asReversed()) {
            if (drawable.onKeyPress(event)) {
                return true
            }
        }

        return false
    }

    override fun onKeyRelease(event: KeyEvent): Boolean {
        for (drawable in drawableList.asReversed()) {
            if (drawable.onKeyRelease(event)) {
                return true
            }
        }

        return false
    }

    override fun onResize(size: Dimension) {
        for (drawable in drawableList) {
            drawable.onResize(size)
        }
    }

    override fun onUserTransformation() {
        for (drawable in drawableList) {
            drawable.onUserTransformation()
        }
    }

    override fun onAttach(plotter: IPlotter) {
        for (drawable in drawableList) {
            drawable.onAttach(plotter)
        }
    }

    override fun onDetach(plotter: IPlotter) {
        for (drawable in drawableList) {
            drawable.onDetach(plotter)
        }
    }
}
