package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.view.base.BaseView
import de.robolab.client.renderer.view.base.IView
import de.robolab.common.utils.Point

class GroupTransformView(
    tag: String?,
    val contextTransformation: (DrawContext) -> DrawContext
) : BaseView(tag) {

    constructor(tag: String?, contextTransformation: (DrawContext) -> DrawContext, vararg viewList: IView) : this(
        tag,
        contextTransformation
    ) {
        addAll(viewList)
    }

    override fun checkPoint(planetPoint: Point, canvasPoint: Point, epsilon: Double): Boolean {
        return false
    }

    override fun onDraw(context: DrawContext) {
        super.onDraw(contextTransformation(context))
    }
}
