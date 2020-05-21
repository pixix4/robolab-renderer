package de.robolab.renderer.drawable.planet

import de.robolab.planet.Planet
import de.robolab.renderer.document.GroupTransformView
import de.robolab.renderer.document.base.menu
import de.robolab.renderer.drawable.edit.CreatePathManager
import de.robolab.renderer.drawable.edit.IEditCallback
import de.robolab.renderer.drawable.general.*
import de.robolab.renderer.utils.DrawContext
import de.westermann.kobserve.base.ObservableValue

class EditPlanetLayer(
        editProperty: ObservableValue<IEditCallback?>,
        createPath: CreatePathManager,
        contextTransformation: (DrawContext) -> DrawContext = { it }
) : IPlanetLayer {

    private val targetManager = TargetAnimatableManager()
    private val senderManager = SenderAnimatableManager()
    private val pathManager = PathAnimatableManager(editProperty)
    private val pathSelectManager = PathSelectAnimatableManager()
    private val pointManager = PointAnimatableManager(editProperty, createPath)
    private val commentManager = CommentAnimatableManager(editProperty)

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
