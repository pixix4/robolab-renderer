package de.robolab.client.renderer.drawable.base

import de.robolab.client.renderer.view.base.IView
import de.robolab.common.planet.Planet

interface IAnimatableManager {

    val view: IView

    fun importPlanet(planet: Planet)
}
