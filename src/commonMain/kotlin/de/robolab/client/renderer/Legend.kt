package de.robolab.client.renderer

import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.canvas.TransformationCanvas
import de.robolab.client.renderer.drawable.planet.AbsPlanetDrawable
import de.robolab.client.renderer.drawable.planet.SimplePlanetDrawable
import de.robolab.client.renderer.plotter.PlotterWindow
import de.robolab.client.renderer.utils.Transformation
import de.robolab.client.theme.utils.ITheme
import de.robolab.client.theme.DefaultLightTheme
import de.robolab.client.utils.HeadlessPlanetDocument
import de.robolab.common.planet.Planet
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Rectangle

object Legend {

    fun renderToCanvas(
        canvas: ICanvas,
        theme: ITheme = DefaultLightTheme
    ) {
        val drawable = SimplePlanetDrawable()
        val planetDocument = HeadlessPlanetDocument(drawable.view)
        val plotter = PlotterWindow(canvas, planetDocument, theme, 0.0)

        drawable.centerPlanet(forceAutoScale = true)
        drawable.drawLegend = true

        plotter.onRender(0.0)
    }

    fun getDimension(planet: Planet): Dimension {
        val rect = AbsPlanetDrawable.calcPlanetArea(planet)?.expand(0.99) ?: Rectangle.ZERO
        return Dimension(rect.width * Transformation.PIXEL_PER_UNIT, rect.height * Transformation.PIXEL_PER_UNIT)
    }

    fun getDimension(drawable: AbsPlanetDrawable): Dimension {
        val rect = drawable.calcPlanetArea()?.expand(0.99) ?: Rectangle.ZERO
        return Dimension(rect.width * Transformation.PIXEL_PER_UNIT, rect.height * Transformation.PIXEL_PER_UNIT)
    }
}
