package de.robolab.app.model.file

import de.robolab.app.model.ISideBarGroup
import de.robolab.app.model.ISideBarPlottable
import de.robolab.renderer.ExportPlotter
import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.drawable.BackgroundDrawable
import de.robolab.renderer.drawable.planet.EditPlanetDrawable
import de.robolab.renderer.drawable.planet.SimplePlanetDrawable
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.utils.SvgCanvas
import de.robolab.renderer.utils.Transformation
import de.westermann.kobserve.not
import de.westermann.kobserve.property.property

class FilePlanetEntry(val filename: String, private val provider: FilePlanetProvider) : ISideBarPlottable {

    private val planetFile = PlanetFile("")

    override val enabledProperty = property(false)

    override val drawable = EditPlanetDrawable()

    override val actionList: List<List<ISideBarPlottable.PlottableAction>> = listOf(
            listOf(
                    ISideBarPlottable.PlottableAction("View", !drawable.editableProperty) {
                        drawable.editableProperty.value = false
                    },
                    ISideBarPlottable.PlottableAction("Edit", drawable.editableProperty) {
                        drawable.editableProperty.value = true
                    }
            ),
            listOf(
                    ISideBarPlottable.PlottableAction("Export SVG") {
                        var name = planetFile.planet.name.trim()
                        if (name.isEmpty()) {
                            name = "export"
                        }
                        saveExportSVG(name, exportSVG())
                    }
            ),
            listOf(
                    ISideBarPlottable.PlottableAction("Export PNG") {
                        var name = planetFile.planet.name.trim()
                        if (name.isEmpty()) {
                            name = "export"
                        }
                        saveExportPNG(name, exportPNG())
                    }
            )
    )

    val content: String
        get() = planetFile.content

    override val canUndoProperty = planetFile.history.canUndoProperty
    override fun undo() {
        planetFile.history.undo()
    }

    override val canRedoProperty = planetFile.history.canRedoProperty
    override fun redo() {
        planetFile.history.redo()
    }

    override fun onOpen() {
        if (!enabledProperty.value) {
            provider.loadEntry(this) { content ->
                if (content != null) {
                    planetFile.resetContent(content)
                    enabledProperty.value = true
                }
            }
        }
    }

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
        val rect = BackgroundDrawable.calcPlanetArea(planetFile.planet)?.expand(0.99) ?: Rectangle.ZERO
        return Dimension(rect.width * Transformation.PIXEL_PER_UNIT, rect.height * Transformation.PIXEL_PER_UNIT)
    }

    private fun exportRender(canvas: ICanvas) {
        val drawable = SimplePlanetDrawable()
        drawable.drawCompass = false
        drawable.drawName = true
        drawable.importPlanet(planetFile.planet)

        val plotter = ExportPlotter(canvas, drawable)

        drawable.centerPlanet()

        plotter.render(0.0)
    }

    override val titleProperty = property(enabledProperty, planetFile.planetProperty) {
        if (enabledProperty.value) {
            val name = planetFile.planet.name
            if (name.isBlank()) "[Unnamed]" else name
        } else {
            filename
        }
    }

    override val subtitleProperty = property(enabledProperty, planetFile.planetProperty) {
        if (enabledProperty.value) "Contains ${planetFile.planet.pathList.size} paths" else "Click to load"
    }

    override val tabNameProperty = titleProperty

    override val parent: ISideBarGroup? = null

    override val unsavedChangesProperty = planetFile.history.canUndoProperty

    init {
        drawable.editCallback = planetFile

        planetFile.history.onChange {
            drawable.importPlanet(planetFile.planet)
        }
    }
}

expect fun exportPNGCanvas(dimension: Dimension): ICanvas
expect fun saveExportSVG(name: String, content: String)
expect fun saveExportPNG(name: String, canvas: ICanvas)
