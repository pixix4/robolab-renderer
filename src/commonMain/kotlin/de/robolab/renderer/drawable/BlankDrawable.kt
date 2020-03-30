package de.robolab.renderer.drawable

import de.robolab.renderer.DefaultPlotter
import de.robolab.renderer.utils.DrawContext
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.base.IDrawable

/**
 * This object does nothing...
 * It is the default drawable of a plotter that only shows the background.
 */
object BlankDrawable : IDrawable {
    override fun onUpdate(ms_offset: Double): Boolean {
        return false
    }

    override fun onDraw(context: DrawContext) {
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        return emptyList()
    }

    override fun onAttach(plotter: DefaultPlotter) {
    }

    override fun onDetach(plotter: DefaultPlotter) {
    }
}
