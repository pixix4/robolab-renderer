package de.robolab.client.renderer.drawable.planet

import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.drawable.edit.IEditCallback
import de.robolab.client.renderer.drawable.general.*
import de.robolab.client.renderer.view.component.GroupTransformView
import de.robolab.common.planet.*

class EditPlanetLayer(
    editCallback: IEditCallback,
    contextTransformation: (DrawContext) -> DrawContext = { it }
) : IPlanetLayer {

    private val targetManager = TargetAnimatableManager()
    private val targetLabelManager = TargetLabelAnimatableManager()
    private val senderManager = SenderAnimatableManager()
    private val pathManager = PathAnimatableManager(editCallback)
    private val pathSelectManager = PathSelectAnimatableManager()
    private val pointManager = PointAnimatableManager(editCallback)
    private val commentManager = CommentAnimatableManager(editCallback)

    override val view = GroupTransformView(
        "Edit planet layer",
        contextTransformation,
        targetManager.view,
        senderManager.view,
        pathManager.view,
        pathSelectManager.view,
        pointManager.view,
        targetLabelManager.view,
        commentManager.view
    )


    override var planet: Planet = Planet.EMPTY
        private set

    override fun importPlanet(planet: Planet) {
        this.planet = planet

        targetManager.importPlanet(planet)
        targetLabelManager.importPlanet(planet)
        senderManager.importPlanet(planet)
        pathManager.importPlanet(planet)
        pathSelectManager.importPlanet(planet)
        pointManager.importPlanet(planet)
        commentManager.importPlanet(planet)
    }

    override fun focus(value: IPlanetValue) {
        when (value) {
            is Path -> pathManager.focus(value)
            is Comment -> commentManager.focus(value)
            is TargetPoint -> pointManager.focus(PointAnimatableManager.AttributePoint(value.target, false))
            is Coordinate -> pointManager.focus(PointAnimatableManager.AttributePoint(value, false))
            is PathSelect -> pointManager.focus(PointAnimatableManager.AttributePoint(value.point, false))
            is StartPoint -> pointManager.focus(PointAnimatableManager.AttributePoint(value.point, false))
        }
    }
}
