package de.robolab.renderer.drawable

import de.robolab.renderer.DrawContext

interface IDrawable {

    fun onUpdate(ms_offset: Double): Boolean

    fun onDraw(context: DrawContext)
}