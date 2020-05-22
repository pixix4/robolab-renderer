package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.view.base.BaseView
import de.robolab.client.renderer.view.base.IView
import de.robolab.common.utils.Point
import de.westermann.kobserve.base.ObservableValue

class ConditionalView(
        tag: String? = null,
        private val drawProperty: ObservableValue<Boolean>,
        private val view: IView
) : BaseView(tag) {

    override fun checkPoint(planetPoint: Point, canvasPoint: Point, epsilon: Double): Boolean {
        return false
    }

    override fun toString(): String {
        val t = tag
        val name = this::class.simpleName ?: return ""
        return if (t == null) "$name(${drawProperty.value})" else "$name($t, ${drawProperty.value})"
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
