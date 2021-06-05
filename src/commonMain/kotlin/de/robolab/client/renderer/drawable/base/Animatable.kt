package de.robolab.client.renderer.drawable.base

import de.robolab.client.renderer.view.base.IView
import de.robolab.client.renderer.view.base.extraPut
import de.robolab.common.planet.utils.IPlanetValue
import de.robolab.common.planet.Planet

abstract class Animatable<T>(
    var reference: T
) {

    abstract val view: IView

    fun onCreate(parent: IView) {
        val obj = reference
        if (obj is IPlanetValue<*>) {
            view.extraPut<IPlanetValue<*>>(obj)
        }

        parent += view
    }

    fun onDestroy(parent: IView) {
        parent -= view
    }

    open fun onUpdate(obj: T, planet: Planet) {
        reference = obj
        if (obj is IPlanetValue<*>) {
            view.extraPut<IPlanetValue<*>>(obj)
        }
    }

    fun focus() {
        view.focus()
    }
}
