package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.view.base.BaseView
import de.robolab.client.renderer.view.base.IView
import de.robolab.common.utils.Vector
import de.westermann.kobserve.base.ObservableValue

class ConditionalView(
    tag: String? = null,
    private val drawProperty: ObservableValue<Boolean>,
    private val view: IView
) : BaseView(tag) {

    override fun checkPoint(planetPoint: Vector, canvasPoint: Vector, epsilon: Double): Boolean {
        return false
    }

    override fun debugStringParameter(): List<Any?> {
        return listOf(drawProperty.value)
    }

    init {
        drawProperty.onChange {
            if (drawProperty.value) {
                add(view)
            } else {
                remove(view)
            }
        }

        if (drawProperty.value) {
            add(view)
        }
    }
}
