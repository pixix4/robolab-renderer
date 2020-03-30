package de.robolab.renderer.drawable.base

import de.robolab.renderer.utils.DrawContext
import de.robolab.renderer.data.Point
import de.robolab.renderer.platform.KeyEvent
import de.robolab.renderer.platform.PointerEvent

abstract class GroupDrawable() : IDrawable {

    abstract val drawableList: List<IDrawable>

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

    override fun onPointerDown(event: PointerEvent): Boolean {
        for (drawable in drawableList.asReversed()) {
            if (drawable.onPointerDown(event)) {
                return true
            }
        }

        return false
    }

    override fun onPointerUp(event: PointerEvent): Boolean {
        var returnValue = false
        for (drawable in drawableList.asReversed()) {
            if (drawable.onPointerUp(event)) {
                returnValue = true
            }
        }

        return returnValue
    }

    override fun onPointerMove(event: PointerEvent): Boolean {
        for (drawable in drawableList.asReversed()) {
            if (drawable.onPointerMove(event)) {
                return true
            }
        }

        return false
    }

    override fun onPointerDrag(event: PointerEvent): Boolean {
        for (drawable in drawableList.asReversed()) {
            if (drawable.onPointerDrag(event)) {
                return true
            }
        }

        return false
    }

    override fun onPointerSecondaryAction(event: PointerEvent): Boolean {
        for (drawable in drawableList.asReversed()) {
            if (drawable.onPointerSecondaryAction(event)) {
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
}
