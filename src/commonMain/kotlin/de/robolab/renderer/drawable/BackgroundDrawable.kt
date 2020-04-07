package de.robolab.renderer.drawable

import de.robolab.planet.Planet
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.animation.ValueTransition
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.drawable.base.IAnimationTime
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.drawable.general.PathAnimatable
import de.robolab.renderer.drawable.utils.BSpline
import de.robolab.renderer.utils.DrawContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class BackgroundDrawable(
        private val planetDrawable: IAnimationTime
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

    fun importPlanet(vararg planet: Planet) = importPlanet(planet.toList())

    fun importPlanet(planetList: List<Planet>) {
        val area = calcPlanetArea(planetList)?.expand(1.0)

        if (area == null) {
            areaTransition.animate(centerRect(areaTransition.value), planetDrawable.animationTime)
            alphaTransition.animate(0.0, planetDrawable.animationTime)

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
        fun calcPlanetArea(planetList: List<Planet>): Rectangle? {
            val areaList = planetList.mapNotNull { calcPlanetArea(it) }
            return if (areaList.isEmpty()) {
                null
            } else {
                areaList.reduce { acc, rectangle ->
                    acc.union(rectangle)
                }
            }
        }
        
        fun calcPlanetArea(planet: Planet): Rectangle? {
            var minX = Double.MAX_VALUE
            var minY = Double.MAX_VALUE
            var maxX = -Double.MAX_VALUE
            var maxY = -Double.MAX_VALUE
            var found = false

            fun update(x: Double, y: Double) {
                minX = min(minX, x)
                minY = min(minY, y)
                maxX = max(maxX, x)
                maxY = max(maxY, y)
                found = true
            }

            for (p in planet.pathList) {
                if (p.hidden) continue

                update(p.source.x.toDouble(), p.source.y.toDouble())
                update(p.target.x.toDouble(), p.target.y.toDouble())

                for (e in p.exposure) {
                    update(e.x.toDouble(), e.y.toDouble())
                }

                val controlPoints = PathAnimatable.getControlPointsFromPath(p)
                val points = PathAnimatable.multiEval(16, controlPoints, Point(p.source), Point(p.target)) {
                    BSpline.eval(it, controlPoints)
                }
                for (c in points) {
                    update(c.left, c.top)
                }
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
