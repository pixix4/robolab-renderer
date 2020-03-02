package de.robolab.renderer.drawable

import de.robolab.renderer.DrawContext
import de.robolab.renderer.data.Point

class GroupDrawable(var drawableList: List<IDrawable>): IDrawable {

    constructor(vararg drawableList: IDrawable): this(drawableList.toList())

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

    override fun getObjectAtPosition(context: DrawContext, position: Point): Any? {
        for (drawable in drawableList.asReversed()) {
            val obj = drawable.getObjectAtPosition(context, position)
            if (obj != null) {
                return obj
            }
        }
        
        return null
    }
}