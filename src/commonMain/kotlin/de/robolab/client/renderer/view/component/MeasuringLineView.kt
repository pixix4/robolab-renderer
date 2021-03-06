package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.view.base.BaseView
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.utils.Vector
import de.robolab.common.utils.Rectangle
import de.robolab.common.utils.toFixed
import de.robolab.common.utils.unionNullable
import de.westermann.kobserve.property.join

class MeasuringLineView(
    source: Vector,
    target: Vector,
    private val accumulator: (Double) -> Double = { it }
) : BaseView() {

    private val precision by PreferenceStorage.paperPrecisionProperty
    private val gridWidth by PreferenceStorage.paperGridWidthProperty

    val sourceTransition = transition(source)
    val source by sourceTransition
    fun setSource(source: Vector, duration: Double = animationTime, offset: Double = 0.0) {
        sourceTransition.animate(source, duration, offset)
    }

    val targetTransition = transition(target)
    val target by targetTransition
    fun setTarget(target: Vector, duration: Double = animationTime, offset: Double = 0.0) {
        targetTransition.animate(target, duration, offset)
    }

    private val distanceStringProperty = sourceTransition.join(targetTransition) { s, t ->
        val distance = (s distanceTo t) * gridWidth
        val accDistance = accumulator(distance)
        accDistance.toFixed(precision) + "m"
    }

    private val labelPositionProperty = sourceTransition.join(targetTransition) { s, t ->
        val endingDirection = (t - s).orthogonal().normalize() * MEASURING_LINE_ENDING_WIDTH
        s.interpolate(t, 0.5) + endingDirection
    }

    override fun onDraw(context: DrawContext) {
        val endingDirection = (target - source).orthogonal().normalize() * MEASURING_LINE_ENDING_WIDTH / 2
        context.strokeLine(
                listOf(
                        source,
                        target
                ),
                context.theme.plotter.lineColor,
                PlottingConstraints.LINE_WIDTH / 2
        )
        context.strokeLine(
                listOf(
                        source + endingDirection,
                        source - endingDirection
                ),
                context.theme.plotter.lineColor,
                PlottingConstraints.LINE_WIDTH / 2
        )
        context.strokeLine(
                listOf(
                        target + endingDirection,
                        target - endingDirection
                ),
                context.theme.plotter.lineColor,
                PlottingConstraints.LINE_WIDTH / 2
        )

        super.onDraw(context)
    }

    override fun calculateBoundingBox(): Rectangle? {
        val parentBox = super.calculateBoundingBox()
        return Rectangle.fromEdges(source, target).expand(PlottingConstraints.LINE_WIDTH / 2) unionNullable parentBox
    }

    override fun checkPoint(planetPoint: Vector, canvasPoint: Vector, epsilon: Double): Boolean {
        val lineVec = target - source
        val pointVec = planetPoint - source

        val (distance, projection) = pointVec projectOnto lineVec

        return when {
            distance < 0.0 -> source
            distance > 1.0 -> target
            else -> projection
        }.distanceTo(planetPoint) - epsilon < PlottingConstraints.LINE_WIDTH / 2
    }
    private val labelView = TextView(
            labelPositionProperty.value,
            12.0,
            distanceStringProperty.value,
            ViewColor.LINE_COLOR,
            ICanvas.FontAlignment.CENTER,
            ICanvas.FontWeight.NORMAL
    )

    override fun debugStringParameter(): List<Any?> {
        return listOf(source, target)
    }

    init {
        add(labelView)

        labelPositionProperty.onChange {
            labelView.setSource(labelPositionProperty.value, 0.0)
        }
        distanceStringProperty.onChange {
            labelView.text = distanceStringProperty.value
        }
    }

    companion object {
        const val MEASURING_LINE_ENDING_WIDTH: Double = 0.12
    }
}
