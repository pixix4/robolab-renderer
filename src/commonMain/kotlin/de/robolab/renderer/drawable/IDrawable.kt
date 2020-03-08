package de.robolab.renderer.drawable

import de.robolab.renderer.DrawContext
import de.robolab.renderer.data.Point

interface IDrawable {

    fun onUpdate(ms_offset: Double): Boolean

    fun onDraw(context: DrawContext)

    fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any>
}
