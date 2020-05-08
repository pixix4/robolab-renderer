package de.robolab.renderer.drawable

import de.robolab.renderer.IPlotter
import de.robolab.renderer.ITransformationReference
import de.robolab.renderer.data.Dimension
import de.robolab.renderer.drawable.base.GroupDrawable
import de.robolab.renderer.utils.Transformation

class BlankDrawable() : GroupDrawable(), ITransformationReference {

    override val drawableList = listOf(
            GridLinesDrawable,
            GridNumbersDrawable,
            CompassDrawable(this)
    )

    var plotter: IPlotter? = null

    override val transformation: Transformation?
        get() = plotter?.transformation

    override fun onAttach(plotter: IPlotter) {
        this.plotter = plotter
    }

    override fun onDetach(plotter: IPlotter) {
        this.plotter = null
    }

    override var autoCentering = true

    override fun centerPlanet(duration: Double) {
        transformation?.translateTo((plotter?.size ?: Dimension.ZERO) / 2, duration)
    }

    override fun onResize(size: Dimension) {
        if (autoCentering) {
            centerPlanet()
        }
    }
}
