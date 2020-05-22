package de.robolab.client.renderer.drawable.planet

import de.robolab.client.renderer.view.base.IView
import de.robolab.common.planet.Planet

interface IPlanetLayer {
    
    val view: IView
    
    val planet: Planet
    
    fun importPlanet(planet: Planet)
}