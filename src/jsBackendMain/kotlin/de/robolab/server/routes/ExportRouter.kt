@file:Suppress("UnsafeCastFromDynamic")

package de.robolab.server.routes

import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.canvas.SvgCanvas
import de.robolab.client.renderer.drawable.planet.AbsPlanetDrawable
import de.robolab.client.renderer.drawable.planet.SimplePlanetDrawable
import de.robolab.client.renderer.plotter.PlotterWindow
import de.robolab.client.renderer.utils.ServerCanvas
import de.robolab.client.renderer.utils.Transformation
import de.robolab.client.theme.LightTheme
import de.robolab.client.utils.HeadlessPlanetDocument
import de.robolab.common.parser.PlanetFile
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Rectangle
import de.robolab.server.externaljs.express.DefaultRouter
import de.robolab.server.externaljs.express.Response
import de.robolab.server.externaljs.express.createRouter
import de.robolab.server.externaljs.express.postSuspend

object ExportRouter {
    val router: DefaultRouter = createRouter()

    init {
        router.postSuspend("/png") { req, res ->
            var scale: Double? = null
            if (req.query.scale) {
                val numberString = req.query.scale.toString()
                val number = numberString.toDoubleOrNull()
                if (number != null) {
                    scale = number
                } else {
                    throw IllegalArgumentException("'$numberString' is not a valid number!")
                }
            }

            console.log(req.files)
            val fileContent = req.files.planet.data.toString().unsafeCast<String>()

            exportPlanetAsPng(PlanetFile(fileContent), scale, res)
        }

        router.postSuspend("/svg") { req, res ->
            val fileContent = req.files.planet.data.toString().unsafeCast<String>()

            exportPlanetAsSvg(PlanetFile(fileContent), res)
        }
    }

    fun exportPlanetAsPng(planetFile: PlanetFile, scale: Double?, res: Response<*>) {
        val exportSize = getDimension(planetFile)

        val s = scale ?: 4.0
        if (s < 0.1 || s > 20.0) {
            throw IllegalArgumentException("Scale $s is out of range (0.1 <= s <= 20.0)!")
        }

        val canvas = ServerCanvas(exportSize, s)
        exportToCanvas(planetFile, canvas)
        val stream = canvas.canvas.createPNGStream()

        res.set("Content-Type", "image/png");
        res.set("Content-Disposition", "attachment; filename=\"${planetFile.planet.name}.png\"")
        stream.pipe(res.asDynamic())
    }

    fun exportPlanetAsSvg(planetFile: PlanetFile, res: Response<*>) {
        val exportSize = getDimension(planetFile)

        val canvas = SvgCanvas(exportSize)
        exportToCanvas(planetFile, canvas)
        val stream = canvas.buildFile()

        res.set("Content-Type", "image/svg+xml");
        res.set("Content-Disposition", "attachment; filename=\"${planetFile.planet.name}.svg\"")
        res.send(stream)
    }

    private fun exportToCanvas(
        planetFile: PlanetFile,
        canvas: ICanvas
    ) {
        val drawable = SimplePlanetDrawable()
        drawable.drawCompass = false
        drawable.drawName = true
        drawable.importPlanet(planetFile.planet)

        val planetDocument = HeadlessPlanetDocument(drawable.view)
        val plotter = PlotterWindow(canvas, planetDocument, LightTheme, 0.0)

        drawable.centerPlanet()

        plotter.render(0.0)
    }

    private fun getDimension(planetFile: PlanetFile): Dimension {
        val rect = AbsPlanetDrawable.calcPlanetArea(planetFile.planet)?.expand(0.99) ?: Rectangle.ZERO
        return Dimension(rect.width * Transformation.PIXEL_PER_UNIT, rect.height * Transformation.PIXEL_PER_UNIT)
    }
}
