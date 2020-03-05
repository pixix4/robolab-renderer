package de.robolab.drawable

import de.robolab.model.Planet
import de.robolab.renderer.DrawContext
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.drawable.IDrawable
import kotlin.math.ceil
import kotlin.math.floor

class EditControlPointsDrawable(
        private val plotter: EditPlanetDrawable
) : IDrawable {

    private val colorTransition = DoubleTransition(0.0)
    private val sizeTransition = DoubleTransition(0.0)
    private val alphaTransition = DoubleTransition(0.0)

    private val transitions = listOf(colorTransition, sizeTransition, alphaTransition)

    private var oldPlanetIsEvenBlue: Boolean? = null
    private var planetIsEvenBlue: Boolean? = null

    override fun onUpdate(ms_offset: Double): Boolean {
        var hasChanges = false

        for (animatable in transitions) {
            if (animatable.update(ms_offset)) {
                hasChanges = true
            }
        }

        return hasChanges
    }

    override fun onDraw(context: DrawContext) {
        if (sizeTransition.value == 0.0 || alphaTransition.value == 0.0) return

        val size = Point(PlottingConstraints.POINT_SIZE / 2, PlottingConstraints.POINT_SIZE / 2) * sizeTransition.value

        val redColor = context.theme.redColor
                .interpolate(context.theme.secondaryBackgroundColor, COLOR_OPACITY)
                .a(alphaTransition.value)
        val blueColor = context.theme.blueColor
                .interpolate(context.theme.secondaryBackgroundColor, COLOR_OPACITY)
                .a(alphaTransition.value)
        val greyColor = context.theme.gridTextColor
                .interpolate(context.theme.secondaryBackgroundColor, COLOR_OPACITY)
                .a(alphaTransition.value)

        for (x in floor(context.area.left).toInt()..ceil(context.area.right).toInt()) {
            for (y in floor(context.area.top).toInt()..ceil(context.area.bottom).toInt()) {
                val isThisPointEven = (x + y) % 2 == 0
                val position = Point(x.toDouble(), y.toDouble())

                val oldColor = oldPlanetIsEvenBlue?.let {
                    if (it == isThisPointEven) {
                        blueColor
                    } else {
                        redColor
                    }
                } ?: greyColor

                val newColor = planetIsEvenBlue?.let {
                    if (it == isThisPointEven) {
                        blueColor
                    } else {
                        redColor
                    }
                } ?: greyColor

                context.fillRect(Rectangle.fromEdges(
                        position - size,
                        position + size
                ), oldColor.interpolate(newColor, colorTransition.value))
            }
        }
    }

    override fun getObjectAtPosition(context: DrawContext, position: Point): Any? {
        return null
    }

    fun startExitAnimation(onFinish: () -> Unit) {
        sizeTransition.animate(0.0, plotter.animationTime / 2, plotter.animationTime / 2)
        sizeTransition.onFinish.clearListeners()
        sizeTransition.onFinish { onFinish() }

        alphaTransition.animate(0.0, plotter.animationTime / 2, plotter.animationTime / 2)
    }

    fun startEnterAnimation(onFinish: () -> Unit) {
        sizeTransition.animate(1.0, plotter.animationTime / 2, plotter.animationTime / 2)
        sizeTransition.onFinish.clearListeners()
        sizeTransition.onFinish { onFinish() }

        alphaTransition.animate(1.0, plotter.animationTime / 2, plotter.animationTime / 2)
    }

    fun importPlanet(planet: Planet) {
        oldPlanetIsEvenBlue = planetIsEvenBlue
        planetIsEvenBlue = if (planet.isStartBlue) {
            (planet.startPoint.first + planet.startPoint.second) % 2 == 0
        } else {
            (planet.startPoint.first + planet.startPoint.second) % 2 == 1
        }

        colorTransition.resetValue(0.0)
        colorTransition.animate(1.0, plotter.animationTime / 2, plotter.animationTime / 4)
    }

    companion object {
        private const val COLOR_OPACITY = 0.85
    }
}
