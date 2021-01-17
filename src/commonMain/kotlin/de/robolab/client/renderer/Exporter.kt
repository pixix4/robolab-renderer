package de.robolab.client.renderer

import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.drawable.planet.AbsPlanetDrawable
import de.robolab.client.renderer.drawable.planet.SimplePlanetDrawable
import de.robolab.client.renderer.plotter.PlotterWindow
import de.robolab.client.renderer.utils.Transformation
import de.robolab.client.theme.LightTheme
import de.robolab.client.utils.HeadlessPlanetDocument
import de.robolab.common.parser.PlanetFile
import de.robolab.common.planet.Planet
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Rectangle

object Exporter {

    fun renderToCanvas(
        planet: Planet,
        canvas: ICanvas,
        drawName: Boolean = true,
        drawNumbers: Boolean = true
    ) {
        val drawable = SimplePlanetDrawable()
        drawable.drawCompass = false
        drawable.drawName = drawName
        drawable.drawGridNumbers = drawNumbers
        drawable.importPlanet(planet)

        val planetDocument = HeadlessPlanetDocument(drawable.view)
        val plotter = PlotterWindow(canvas, planetDocument, LightTheme, 0.0)

        drawable.centerPlanet()

        plotter.render(0.0)
    }

    fun getExportName(planet: Planet, format: String): String {
        return planet.name.let { if (it.isEmpty()) "planet" else it } + "." + format
    }


    fun getDimension(planet: Planet): Dimension {
        val rect = AbsPlanetDrawable.calcPlanetArea(planet)?.expand(0.99) ?: Rectangle.ZERO
        return Dimension(rect.width * Transformation.PIXEL_PER_UNIT, rect.height * Transformation.PIXEL_PER_UNIT)
    }
}

