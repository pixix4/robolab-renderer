package de.robolab.renderer.document

import de.robolab.renderer.data.Point
import de.robolab.renderer.document.base.BaseView
import de.robolab.renderer.document.base.IView
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
        if (drawProperty.value) {
            add(view)
        }

        drawProperty.onChange {
            if (drawProperty.value) {
                add(view)
            } else {
                remove(view)
            }
        }
    }
}
