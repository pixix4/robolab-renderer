package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.view.base.BaseView
import de.robolab.client.renderer.view.base.IView
import de.robolab.common.utils.Vector

class GroupView(tag: String?): BaseView(tag) {
    
    constructor(tag: String?, vararg viewList: IView): this(tag) {
        addAll(viewList)
    }

    override fun checkPoint(planetPoint: Vector, canvasPoint: Vector, epsilon: Double): Boolean {
        return false
    }
}
