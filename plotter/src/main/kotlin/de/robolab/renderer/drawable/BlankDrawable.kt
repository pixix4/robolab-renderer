package de.robolab.renderer.drawable

import de.robolab.renderer.DrawContext

object BlankDrawable: IDrawable {
    override fun onUpdate(ms_offset: Double): Boolean {
        return false
    }

    override fun onDraw(context: DrawContext) {
    }
}