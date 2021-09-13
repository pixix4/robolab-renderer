package de.robolab.client.renderer.drawable.planet

import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.drawable.general.*
import de.robolab.client.renderer.drawable.utils.PlanetRequestContext
import de.robolab.client.renderer.view.component.GroupTransformView
import de.robolab.common.planet.Planet

class PlanetLayer(
    name: String,
    requestContext: PlanetRequestContext,
    contextTransformation: (DrawContext) -> DrawContext = { it }
) : IPlanetLayer {

    private val targetManager = TargetAnimatableManager()
    private val targetLabelManager = TargetLabelAnimatableManager()
    private val senderManager = SenderAnimatableManager()
    private val pathManager = PathAnimatableManager(requestContext = requestContext)
    private val pathSelectManager = PathSelectAnimatableManager()
    private val pointManager = PointAnimatableManager(requestContext = requestContext)
    private val commentManager = CommentAnimatableManager()

    override val view = GroupTransformView(
        name,
        contextTransformation,
        targetManager,
        senderManager,
        pathManager,
        pathSelectManager,
        pointManager,
        targetLabelManager,
        commentManager
    )


    override var planet: Planet = Planet.EMPTY
        private set

    override fun importPlanet(planet: Planet) {
        this.planet = planet

        view.importPlanet(planet)
    }
}
