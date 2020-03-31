package de.robolab.renderer.utils

import de.robolab.renderer.data.Color
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.theme.ITheme

class DrawContext(
        val canvas: ICanvas,
        val transformation: Transformation,
        var theme: ITheme
) : ICanvas by TransformationCanvas(ColorCanvas(canvas, { it }), transformation) {

    private var alphaFactor: Double = 1.0
    private fun c(color: Color): Color {
        return if (alphaFactor > 1.0) {
            color.a(color.alpha * alphaFactor)
        } else color
    }

    fun withAlpha(alphaFactor: Double, block: () -> Unit) {
        val oldAlphaFactor = alphaFactor
        this.alphaFactor = alphaFactor
        block()
        this.alphaFactor = oldAlphaFactor
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
