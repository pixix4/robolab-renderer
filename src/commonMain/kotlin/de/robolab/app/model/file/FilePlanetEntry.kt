package de.robolab.app.model.file

import de.robolab.app.model.IPlottable
import de.robolab.renderer.ExportPlotter
import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.drawable.BackgroundDrawable
import de.robolab.renderer.drawable.planet.EditPlanetDrawable
import de.robolab.renderer.drawable.planet.SimplePlanetDrawable
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.utils.SvgCanvas
import de.robolab.renderer.utils.Transformation
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.not
import de.westermann.kobserve.property.mapBinding

class FilePlanetEntry(private val filename: String, content: String) : IPlottable {

    private val planetFile = PlanetFile(content)

    override val drawable = EditPlanetDrawable()

    override val actionList: List<List<IPlottable.PlottableAction>> = listOf(
            listOf(
                    IPlottable.PlottableAction("View", !drawable.editableProperty) {
                        drawable.editableProperty.value = false
                    },
                    IPlottable.PlottableAction("Edit", drawable.editableProperty) {
                        drawable.editableProperty.value = true
                    }
            ),
            listOf(
                    IPlottable.PlottableAction("Export SVG") {
                        var name = planetFile.planet.value.name.trim()
                        if (name.isEmpty()) {
                            name = "export"
                        }
                        saveExportSVG(name, exportSVG())
                    }
            ),
            listOf(
                    IPlottable.PlottableAction("Export PNG") {
                        var name = planetFile.planet.value.name.trim()
                        if (name.isEmpty()) {
                            name = "export"
                        }
                        saveExportPNG(name, exportPNG())
                    }
            )
    )


    private fun exportSVG(): String {
        val dimension = exportGetSize()
        val canvas = SvgCanvas(dimension.width, dimension.height)

        exportRender(canvas)

        return canvas.buildFile()
    }

    private fun exportPNG(): ICanvas {
        val canvas = exportPNGCanvas(exportGetSize())

        exportRender(canvas)

        return canvas
    }

    private fun exportGetSize(): Dimension {
        val rect = BackgroundDrawable.calcPlanetArea(planetFile.planet.value)?.expand(0.99) ?: Rectangle.ZERO
        return Dimension(rect.width * Transformation.PIXEL_PER_UNIT, rect.height * Transformation.PIXEL_PER_UNIT)
    }

    private fun exportRender(canvas: ICanvas) {
        val drawable = SimplePlanetDrawable()
        drawable.drawCompass = false
        drawable.drawName = true
        drawable.importPlanet(planetFile.planet.value)

        val plotter = ExportPlotter(canvas, drawable)

        drawable.centerPlanet()

        plotter.render(0.0)
    }

    override val nameProperty = planetFile.planet.mapBinding { it.name }

    override val statusProperty = planetFile.planet.mapBinding { "Contains ${it.pathList.size} paths" }

    override val unsavedChangesProperty = planetFile.history.canUndoProperty

    init {
        drawable.editCallback = planetFile

        planetFile.history.valueProperty.onChange {
            drawable.importPlanet(planetFile.planet.value)
        }
        drawable.importPlanet(planetFile.planet.value)
    }
}

expect fun exportPNGCanvas(dimension: Dimension): ICanvas
expect fun saveExportSVG(name: String, content: String)
expect fun saveExportPNG(name: String, canvas: ICanvas)
