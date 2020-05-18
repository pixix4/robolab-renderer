package de.robolab.renderer.drawable.planet

import de.robolab.planet.Planet
import de.robolab.renderer.document.GroupTransformView
import de.robolab.renderer.drawable.general.*
import de.robolab.renderer.utils.DrawContext

class PlanetLayer(
        contextTransformation: (DrawContext) -> DrawContext = { it }
): IPlanetLayer {

    private val targetManager = TargetAnimatableManager()
    private val senderManager = SenderAnimatableManager()
    private val pathManager = PathAnimatableManager()
    private val pathSelectManager = PathSelectAnimatableManager()
    private val pointManager = PointAnimatableManager()
    private val commentManager = CommentAnimatableManager()

    override val view = GroupTransformView(
            contextTransformation,
            targetManager.view,
            senderManager.view,
            pathManager.view,
            pathSelectManager.view,
            pointManager.view,
            commentManager.view
    )
    

    override var planet: Planet = Planet.EMPTY
        private set

    override fun importPlanet(planet: Planet) {
        this.planet = planet

        targetManager.importPlanet(planet)
        senderManager.importPlanet(planet)
        pathManager.importPlanet(planet)
        pathSelectManager.importPlanet(planet)
        pointManager.importPlanet(planet)
        commentManager.importPlanet(planet)
    }
}
