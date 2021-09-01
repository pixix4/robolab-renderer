package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.drawable.utils.c
import de.robolab.client.renderer.view.base.BaseView
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.common.utils.Vector
import de.robolab.common.utils.Rectangle
import de.robolab.common.utils.unionNullable

class LabeledArrowView(
    source: Vector,
    target: Vector,
    label: Char,
    color: ViewColor,
) : BaseView() {


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

    private var label = label
    fun setLabel(label: Char) {
        this.label = label
        animatableManager.requestRedraw()
    }

    val colorTransition = transition(color)
    val color by colorTransition
    fun setColor(color: ViewColor, duration: Double = animationTime, offset: Double = 0.0) {
        colorTransition.animate(color, duration, offset)
    }

    private var points = emptyList<Vector>()

    private fun updatePoints() {
        val targetToSource = source - target

        val h = targetToSource.magnitude()
        val i = h * 0.2
        val w = h * 0.6

        val side = targetToSource.orthogonal().normalize() * w / 2
        points = listOf(
            source,
            target + side,
            target + targetToSource.normalize() * i,
            target - side,
        )
    }

    init {
        sourceTransition.onChange {
            updatePoints()
        }
        targetTransition.onChange {
            updatePoints()
        }
        updatePoints()
    }

    override fun onDraw(context: DrawContext) {
        context.fillPolygon(points, context.c(color))

        context.fillText(
            label.toString(),
            target + (target - source).normalize() * 0.05,
            context.c(color),
            12.0,
            ICanvas.FontAlignment.CENTER
        )

        super.onDraw(context)
    }

    override fun calculateBoundingBox(): Rectangle? {
        val parentBox = super.calculateBoundingBox()
        return Rectangle.fromEdges(source, target).expand(source.distanceTo(target)) unionNullable parentBox
    }
    
    override fun checkPoint(planetPoint: Vector, canvasPoint: Vector, epsilon: Double): Boolean {
        return false
    }

    override fun onDestroy(onFinish: () -> Unit) {
        setColor(ViewColor.TRANSPARENT)

        animatableManager.onFinish(onFinish)
    }
}
