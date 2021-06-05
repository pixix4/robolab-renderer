package de.robolab.client.renderer.drawable.planet

import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.drawable.edit.IEditCallback
import de.robolab.client.renderer.drawable.general.*
import de.robolab.client.renderer.view.component.GroupTransformView
import de.robolab.common.planet.*
import de.robolab.common.planet.utils.IPlanetValue

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

    override fun focus(value: IPlanetValue<*>) {
        when (value) {
            is PlanetPath -> pathManager.focus(value)
            is PlanetComment -> commentManager.focus(value)
            is PlanetTarget -> pointManager.focus(PointAnimatableManager.AttributePoint(value.point, false))
            is PlanetPoint -> pointManager.focus(PointAnimatableManager.AttributePoint(value, false))
            is PlanetPathSelect -> pointManager.focus(PointAnimatableManager.AttributePoint(value.point, false))
            is PlanetStartPoint -> pointManager.focus(PointAnimatableManager.AttributePoint(value.point, false))
        }
    }
}
