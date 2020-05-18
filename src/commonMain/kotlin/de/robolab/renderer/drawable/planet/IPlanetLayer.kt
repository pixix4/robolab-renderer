package de.robolab.renderer.drawable.planet

import de.robolab.planet.Planet
import de.robolab.renderer.document.base.IView

interface IPlanetLayer {
    
    val view: IView
    
    val planet: Planet
    
    fun importPlanet(planet:Planet)
}