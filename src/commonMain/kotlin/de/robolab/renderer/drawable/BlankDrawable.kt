package de.robolab.renderer.drawable

import de.robolab.renderer.DrawContext
import de.robolab.renderer.data.Point

/**
 * This object does nothing...
 * It is the default drawable of a plotter that only shows the background.
 */
object BlankDrawable: IDrawable {
    override fun onUpdate(ms_offset: Double): Boolean {
        return false
    }

    override fun onDraw(context: DrawContext) {
    }

    override fun getObjectAtPosition(context: DrawContext, position: Point): Any? {
        return null
    }
}
