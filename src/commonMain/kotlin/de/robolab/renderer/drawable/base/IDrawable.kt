package de.robolab.renderer.drawable.base

import de.robolab.renderer.DefaultPlotter
import de.robolab.renderer.DrawContext
import de.robolab.renderer.IInteraction
import de.robolab.renderer.data.Point
import de.robolab.renderer.platform.ICanvasListener

interface IDrawable: IInteraction {

    fun onUpdate(ms_offset: Double): Boolean

    fun onDraw(context: DrawContext)

    fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any>

    fun onAttach(plotter: DefaultPlotter) {

    }

    fun onDetach(plotter: DefaultPlotter) {

    }
}
