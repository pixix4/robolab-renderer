package de.robolab.drawable

import de.robolab.model.Planet
import de.robolab.renderer.DrawContext
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.animation.ValueTransition
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.drawable.IDrawable

class BackgroundDrawable : IDrawable {

    private val areaTransition = ValueTransition(Rectangle.ZERO)
    private val alphaTransition = DoubleTransition(0.0)

    override fun onUpdate(ms_offset: Double): Boolean {
        var changed = alphaTransition.update(ms_offset)

        if (areaTransition.update(ms_offset)) {
            changed = true
        }

        return changed
    }

    override fun onDraw(context: DrawContext) {
        context.fillRect(areaTransition.value, context.theme.primaryBackgroundColor.a(alphaTransition.value))
    }

    fun importPlanet(planet: Planet) {
        val pointList = (
                planet.pathList.flatMap { listOf(it.source, it.target) } +
                        planet.targetList.map { it.target } +
                        planet.targetList.flatMap { it.exposure }
                ).distinct().map { (left, top) ->
            Point(left.toDouble(), top.toDouble())
        }

        if (pointList.isEmpty()) {
            areaTransition.animate(centerRect(areaTransition.value), planet.animationTime)
            alphaTransition.animate(0.0, planet.animationTime)

            return
        }

        val area = Rectangle.fromEdges(pointList).expand(1.0)

        if (alphaTransition.value == 0.0) {
            areaTransition.resetValue(centerRect(area))
        }

        areaTransition.animate(area, planet.animationTime)
        alphaTransition.animate(0.0, planet.animationTime)
    }

    private fun centerRect(rectangle: Rectangle) = Rectangle(
            rectangle.left + rectangle.width / 2,
            rectangle.top + rectangle.height / 2,
            0.0,
            0.0
    )
}
