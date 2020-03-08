package de.robolab.renderer.drawable

import de.robolab.renderer.DrawContext
import de.robolab.renderer.data.Point

class GroupDrawable(private var drawableList: List<IDrawable>): IDrawable {

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

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        return drawableList.asReversed().flatMap { it.getObjectsAtPosition(context, position) }
    }
}