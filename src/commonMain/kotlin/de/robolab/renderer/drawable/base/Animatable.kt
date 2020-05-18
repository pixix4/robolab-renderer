package de.robolab.renderer.drawable.base

import de.robolab.planet.Planet
import de.robolab.renderer.document.base.IView

abstract class Animatable<T>(
        var reference: T
) {

    abstract val view: IView
    
    open fun onCreate(parent: IView) {
        parent += view
    }

    open fun onDestroy(parent: IView) {
        parent -= view
    }

    open fun onUpdate(obj: T, planet: Planet) {
        reference = obj
    }
}
