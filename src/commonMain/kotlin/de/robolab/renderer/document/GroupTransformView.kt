package de.robolab.renderer.document

import de.robolab.renderer.data.Point
import de.robolab.renderer.document.base.BaseView
import de.robolab.renderer.document.base.IView
import de.robolab.renderer.utils.DrawContext

class GroupTransformView(
        val contextTransformation: (DrawContext) -> DrawContext
): BaseView() {
    
    constructor(contextTransformation: (DrawContext) -> DrawContext, vararg viewList: IView): this(contextTransformation) {
        addAll(viewList)
    }

    override fun checkPoint(planetPoint: Point, canvasPoint: Point, epsilon: Double): Boolean {
        return false
    }

    override fun onDraw(context: DrawContext) {
        super.onDraw(contextTransformation(context))
    }
}
