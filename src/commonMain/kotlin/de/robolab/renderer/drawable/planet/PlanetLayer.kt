package de.robolab.renderer.drawable.planet

import de.robolab.planet.Planet
import de.robolab.renderer.drawable.base.GroupDrawable
import de.robolab.renderer.drawable.base.IAnimationTime
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.drawable.general.*
import de.robolab.renderer.utils.Pointer

class PlanetLayer(private val planetDrawable: AbsPlanetDrawable) : GroupDrawable(), IAnimationTime {

    override val animationTime: Double
        get() = planetDrawable.animationTime

    override val selectedElements: List<Any>
        get() = planetDrawable.selectedElements

    override val pointer: Pointer?
        get() = planetDrawable.pointer

    private val targetDrawable = TargetAnimatableManager(this)
    private val senderDrawable = SenderAnimatableManager(this)
    private val pathDrawable = PathAnimatableManager(this)
    private val pathSelectDrawable = PathSelectAnimatableManager(this)
    private val pointDrawable = PointAnimatableManager(this)

    override val drawableList = listOf<IDrawable>(
            targetDrawable,
            senderDrawable,
            pathDrawable,
            pathSelectDrawable,
            pointDrawable
    )

    var planet: Planet = Planet.EMPTY
        private set

    fun importPlanet(planet: Planet) {
        this.planet = planet

        targetDrawable.importPlanet(planet)
        senderDrawable.importPlanet(planet)
        pathDrawable.importPlanet(planet)
        pathSelectDrawable.importPlanet(planet)
        pointDrawable.importPlanet(planet)
    }
}
