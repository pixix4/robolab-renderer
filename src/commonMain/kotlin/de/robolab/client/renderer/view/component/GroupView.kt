package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.view.base.BaseView
import de.robolab.client.renderer.view.base.IView
import de.robolab.common.utils.Point

class GroupView(tag: String?): BaseView(tag) {
    
    constructor(tag: String?, vararg viewList: IView): this(tag) {
        addAll(viewList)
    }

    override fun checkPoint(planetPoint: Point, canvasPoint: Point, epsilon: Double): Boolean {
        return false
    }
}
