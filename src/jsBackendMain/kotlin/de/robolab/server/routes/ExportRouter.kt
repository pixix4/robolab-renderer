@file:Suppress("UnsafeCastFromDynamic")

package de.robolab.server.routes

import de.robolab.client.renderer.Exporter
import de.robolab.client.renderer.canvas.SvgCanvas
import de.robolab.client.renderer.utils.ServerCanvas
import de.robolab.common.parser.PlanetFile
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
        val exportSize = Exporter.getDimension(planetFile.planet)

        val s = scale ?: 4.0
        if (s < 0.1 || s > 20.0) {
            throw IllegalArgumentException("Scale $s is out of range (0.1 <= s <= 20.0)!")
        }

        val canvas = ServerCanvas(exportSize, s)
        Exporter.renderToCanvas(planetFile.planet, canvas)
        val stream = canvas.canvas.createPNGStream()

        res.set("Content-Type", "image/png")
        res.set("Content-Disposition", "attachment; filename=\"${Exporter.getExportName(planetFile.planet, "png")}\"")
        stream.pipe(res.asDynamic())
    }

    fun exportPlanetAsSvg(planetFile: PlanetFile, res: Response<*>) {
        val exportSize = Exporter.getDimension(planetFile.planet)

        val canvas = SvgCanvas(exportSize)
        Exporter.renderToCanvas(planetFile.planet, canvas)
        val stream = canvas.buildFile()

        res.set("Content-Type", "image/svg+xml")
        res.set("Content-Disposition", "attachment; filename=\"${Exporter.getExportName(planetFile.planet, "svg")}\"")
        res.send(stream)
    }
}
