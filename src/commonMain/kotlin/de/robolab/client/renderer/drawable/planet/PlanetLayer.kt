package de.robolab.client.renderer.drawable.planet

import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.drawable.general.*
import de.robolab.client.renderer.view.component.GroupTransformView
import de.robolab.common.planet.Planet

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
