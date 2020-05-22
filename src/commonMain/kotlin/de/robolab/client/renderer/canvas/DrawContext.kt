package de.robolab.client.renderer.canvas

import de.robolab.client.renderer.utils.Transformation
import de.robolab.client.theme.ITheme
import de.robolab.common.utils.Point
import de.robolab.common.utils.Rectangle

class DrawContext(
    val canvas: ICanvas,
    val transformation: Transformation,
    var theme: ITheme,
    var debug: Boolean
) : ICanvas by TransformationCanvas(canvas, transformation) {

    var renderedViewCount = 0

    fun withAlpha(alphaFactor: Double): DrawContext {
        val context = DrawContext(
            ColorCanvas(canvas) {
                theme.plotter.primaryBackgroundColor.interpolate(it, alphaFactor)
            },
            transformation,
            theme,
            debug
        )
        context.area = area
        return context
    }

    var area: Rectangle = Rectangle.ZERO
        private set

    private fun updateArea() {
        area = Rectangle.fromEdges(
            transformation.canvasToPlanet(Point(0.0, 0.0)),
            transformation.canvasToPlanet(Point(canvas.width, 0.0)),
            transformation.canvasToPlanet(Point(0.0, canvas.height)),
            transformation.canvasToPlanet(Point(canvas.width, canvas.height))
        )
    }

    init {
        transformation.onViewChange {
            updateArea()
        }
    }
}
