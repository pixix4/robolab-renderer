package de.robolab.renderer.drawable.general

import de.robolab.planet.PathSelect
import de.robolab.planet.Planet
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Color
import de.robolab.renderer.document.ArrowView
import de.robolab.renderer.document.ViewColor
import de.robolab.renderer.drawable.base.Animatable
import de.robolab.renderer.drawable.utils.toPoint

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
