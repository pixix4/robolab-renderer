package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.drawable.base.Animatable
import de.robolab.client.renderer.drawable.utils.toPoint
import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.client.renderer.view.component.ArrowView
import de.robolab.common.planet.PathSelect
import de.robolab.common.planet.Planet

class PathSelectAnimatable(
        reference: PathSelect
) : Animatable<PathSelect>(reference) {

    override val view: ArrowView

    override fun onUpdate(obj: PathSelect, planet: Planet) {
        super.onUpdate(obj, planet)

        val (source, target) = PathSelectAnimatableManager.getArrow(reference.point.toPoint(), reference.direction)
        view.setSource(source)
        view.setTarget(target)
    }

    init {
        val (source, target) = PathSelectAnimatableManager.getArrow(reference.point.toPoint(), reference.direction)

        view = ArrowView(
                source,
                target,
                PlottingConstraints.LINE_WIDTH * 0.65,
                ViewColor.LINE_COLOR
        )
    }
}
