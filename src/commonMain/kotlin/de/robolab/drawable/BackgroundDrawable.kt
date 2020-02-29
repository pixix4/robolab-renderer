package de.robolab.drawable

import de.robolab.model.Planet
import de.robolab.renderer.Animator
import de.robolab.renderer.DrawContext
import de.robolab.renderer.Plotter
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.drawable.IDrawable

class BackgroundDrawable : IDrawable {

    private var fromArea: Rectangle? = null
    private var targetArea: Rectangle? = null
    private var currentArea: Rectangle? = null
    private var animateAlpha = false
    private var alpha: Double = 0.0

    private val animator = Animator(Plotter.ANIMATION_TIME)

    override fun onUpdate(ms_offset: Double): Boolean {
        val changed = animator.update(ms_offset)

        if (changed) {
            fromArea?.let { fromArea ->
                targetArea?.let {targetArea ->
                    currentArea = Rectangle(
                            fromArea.left + (targetArea.left - fromArea.left) * animator.current,
                            fromArea.top + (targetArea.top - fromArea.top) * animator.current,
                            fromArea.width + (targetArea.width - fromArea.width) * animator.current,
                            fromArea.height + (targetArea.height - fromArea.height) * animator.current
                    )
                    alpha = if (animateAlpha) animator.current else 1.0
                } ?: run {
                    currentArea = fromArea
                    alpha = if (animateAlpha) 1.0 - animator.current else 1.0
                }
            } ?: run {
                currentArea = targetArea
                alpha = if (animateAlpha) animator.current else 1.0
            }

        }

        return changed
    }

    override fun onDraw(context: DrawContext) {
        currentArea?.let { area ->
            context.fillRect(area, context.theme.primaryBackgroundColor.a(alpha))
        }
    }

    fun importPlanet(planet: Planet) {
        val pointList = (
                planet.pathList.flatMap { listOf(it.source, it.target) } +
                        planet.targetList.map { it.target } +
                        planet.targetList.flatMap { it.exposure }
                ).distinct().map { (left, top) ->
            Point(left.toDouble(), top.toDouble())
        }

        val area =  Rectangle.fromEdges(pointList).expand(1.0)

        fromArea = if (currentArea == null) {
            animateAlpha = true
            Rectangle (
                    area.left + area.width / 2,
                    area.top + area.height / 2,
                    0.0,
                    0.0
            )
        } else {
            animateAlpha = false
            currentArea
        }
        targetArea = area
        animator.animate(0.0, 1.0)
    }
}
