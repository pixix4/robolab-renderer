package de.robolab.client.renderer.plotter

import de.robolab.client.app.model.base.EmptyPlanetDocument
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.common.utils.Rectangle
import de.robolab.common.utils.Vector
import de.westermann.kobserve.property.constObservable

class SimplePlotterManager(
    canvas: ICanvas,
    animationTime: Double
) : IPlotterManager(canvas, animationTime) {

    private val plotter = PlotterWindow(canvas, EmptyPlanetDocument(), theme, animationTime)
    override val activePlotter = plotter
    override val activePlotterProperty = constObservable(plotter)

    override val plotterList = listOf(activePlotter)
    private val fpsCounter = FpsCounter()

    override fun onRender(msOffset: Double): Boolean {
        fpsCounter.update(msOffset)

        if (requestRedraw) {
            canvas.fillRect(
                Rectangle.fromDimension(Vector.ZERO, canvas.dimension).expand(1.0),
                theme.plotter.secondaryBackgroundColor
            )
        }

        var hasUpdated = false
        val windowChanged = plotter.onUpdate(msOffset)

        if (windowChanged || requestRedraw) {
            hasUpdated = true
            plotter.onDraw()
            if (debugHierarchy) {
                plotter.onDebugDraw()
            }
        }

        requestRedraw = false

        return hasUpdated
    }
}
