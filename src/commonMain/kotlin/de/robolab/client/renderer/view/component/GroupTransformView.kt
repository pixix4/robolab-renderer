package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.drawable.base.AnimatableManager
import de.robolab.client.renderer.view.base.BaseView
import de.robolab.common.planet.Planet
import de.robolab.common.utils.Vector

class GroupTransformView(
    tag: String?,
    val contextTransformation: (DrawContext) -> DrawContext
) : BaseView(tag) {

    private var animatableManagerList = emptyList<AnimatableManager<*, *>>()

    constructor(
        tag: String?,
        contextTransformation: (DrawContext) -> DrawContext,
        vararg viewList: AnimatableManager<*, *>
    ) : this(
        tag,
        contextTransformation
    ) {
        animatableManagerList = viewList.toList()
        addAll(viewList.map { it.view })
    }

    override fun checkPoint(planetPoint: Vector, canvasPoint: Vector, epsilon: Double): Boolean {
        return false
    }

    override fun onDraw(context: DrawContext) {
        super.onDraw(contextTransformation(context))
    }

    fun importPlanet(planet: Planet) {
        for (manager in animatableManagerList) {
            manager.importPlanet(planet)
        }
    }
}
