package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.view.base.BaseView
import de.robolab.client.renderer.view.base.IView
import de.robolab.common.utils.Point

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
