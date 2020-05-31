package de.robolab.client.renderer.drawable.base

import de.robolab.client.renderer.view.base.IView
import de.robolab.common.planet.Planet

abstract class Animatable<T>(
    var reference: T
) {

    abstract val view: IView

    fun onCreate(parent: IView) {
        parent += view
    }

    fun onDestroy(parent: IView) {
        parent -= view
    }

    open fun onUpdate(obj: T, planet: Planet) {
        reference = obj
    }
}
