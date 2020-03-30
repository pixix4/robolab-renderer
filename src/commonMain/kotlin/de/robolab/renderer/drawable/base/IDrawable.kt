package de.robolab.renderer.drawable.base

import de.robolab.renderer.DefaultPlotter
import de.robolab.renderer.utils.DrawContext
import de.robolab.renderer.IInteraction
import de.robolab.renderer.data.Point

interface IDrawable: IInteraction {

    fun onUpdate(ms_offset: Double): Boolean

    fun onDraw(context: DrawContext)

    fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any>

    fun onAttach(plotter: DefaultPlotter) {

    }

    fun onDetach(plotter: DefaultPlotter) {

    }
}
