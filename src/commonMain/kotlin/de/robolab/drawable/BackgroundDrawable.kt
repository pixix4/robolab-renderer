package de.robolab.drawable

import de.robolab.drawable.curve.BSpline
import de.robolab.model.Planet
import de.robolab.renderer.DrawContext
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.animation.ValueTransition
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.drawable.IDrawable
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class BackgroundDrawable(
        private val planetDrawable: PlanetDrawable
) : IDrawable {

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

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        return emptyList()
    }

    fun importPlanet(planet: Planet) {
        var minX = Double.MAX_VALUE
        var minY = Double.MAX_VALUE
        var maxX = Double.MIN_VALUE
        var maxY = Double.MIN_VALUE
        var found = false

        fun update(x: Double, y: Double) {
            minX = min(minX, x)
            minY = min(minY, y)
            maxX = max(maxX, x)
            maxY = max(maxY, y)
            found = true
        }

        for (p in planet.pathList) {
            update(p.source.first.toDouble(), p.source.second.toDouble())
            update(p.target.first.toDouble(), p.target.second.toDouble())

            for (e in p.exposure) {
                update(e.first.toDouble(), e.second.toDouble())
            }

            val controlPoints = PathDrawable.getControlPointsFromPath(p)
            val points = PathDrawable.multiEval(16, controlPoints, Point(p.source), Point(p.target)) {
                BSpline.eval(it, controlPoints)
            }
            for (c in points) {
                update(c.left, c.top)
            }
        }

        for (t in planet.targetList) {
            update(t.target.first.toDouble(), t.target.second.toDouble())

            for (e in t.exposure) {
                update(e.first.toDouble(), e.second.toDouble())
            }
        }

        for (p in planet.pathSelectList) {
            update(p.point.first.toDouble(), p.point.second.toDouble())
        }

        if (!found) {
            areaTransition.animate(centerRect(areaTransition.value), this.planetDrawable.animationTime)
            alphaTransition.animate(0.0, this.planetDrawable.animationTime)

            return
        }

        minX = round(minX * 20.0) / 20.0
        minY = round(minY * 20.0) / 20.0
        maxX = round(maxX * 20.0) / 20.0
        maxY = round(maxY * 20.0) / 20.0

        val area = Rectangle(minX, minY, maxX - minX, maxY - minY).expand(1.0)

        if (alphaTransition.value == 0.0) {
            areaTransition.resetValue(centerRect(area))
        }

        areaTransition.animate(area, this.planetDrawable.animationTime)
        alphaTransition.animate(1.0, this.planetDrawable.animationTime)
    }

    private fun centerRect(rectangle: Rectangle) = Rectangle(
            rectangle.left + rectangle.width / 2,
            rectangle.top + rectangle.height / 2,
            0.0,
            0.0
    )
}
