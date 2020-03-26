package de.robolab.renderer.drawable

import de.robolab.model.Planet
import de.robolab.renderer.DrawContext
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.animation.ValueTransition
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.drawable.curve.BSpline
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
        val area = calcPlanetArea(planet)?.expand(1.0)

        if (area == null) {
            areaTransition.animate(centerRect(areaTransition.value), this.planetDrawable.animationTime)
            alphaTransition.animate(0.0, this.planetDrawable.animationTime)

            return
        }

        if (alphaTransition.value == 0.0) {
            areaTransition.resetValue(centerRect(area))
        }

        areaTransition.animate(area, this.planetDrawable.animationTime)
        alphaTransition.animate(1.0, this.planetDrawable.animationTime)
    }

    private fun centerRect(rectangle: Rectangle) = Rectangle.fromEdges(rectangle.center)
    
    companion object {
        fun calcPlanetArea(planet: Planet): Rectangle? {
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
                update(p.source.x.toDouble(), p.source.y.toDouble())
                update(p.target.x.toDouble(), p.target.y.toDouble())

                for (e in p.exposure) {
                    update(e.x.toDouble(), e.y.toDouble())
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
                update(t.target.x.toDouble(), t.target.y.toDouble())
                update(t.exposure.x.toDouble(), t.exposure.y.toDouble())
            }

            for (p in planet.pathSelectList) {
                update(p.point.x.toDouble(), p.point.y.toDouble())
            }

            if (!found) {
                return null
            }

            minX = round(minX * PlottingConstraints.PRECISION_FACTOR) / PlottingConstraints.PRECISION_FACTOR
            minY = round(minY * PlottingConstraints.PRECISION_FACTOR) / PlottingConstraints.PRECISION_FACTOR
            maxX = round(maxX * PlottingConstraints.PRECISION_FACTOR) / PlottingConstraints.PRECISION_FACTOR
            maxY = round(maxY * PlottingConstraints.PRECISION_FACTOR) / PlottingConstraints.PRECISION_FACTOR

            return Rectangle(minX, minY, maxX - minX, maxY - minY)
        }
    }
}
