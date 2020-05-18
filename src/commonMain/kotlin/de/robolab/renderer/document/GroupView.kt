package de.robolab.renderer.document

import de.robolab.renderer.data.Point
import de.robolab.renderer.document.base.BaseView
import de.robolab.renderer.document.base.IView

class GroupView(tag: String?): BaseView(tag) {
    
    constructor(tag: String?, vararg viewList: IView): this(tag) {
        addAll(viewList)
    }

    override fun checkPoint(planetPoint: Point, canvasPoint: Point, epsilon: Double): Boolean {
        return false
    }
}
