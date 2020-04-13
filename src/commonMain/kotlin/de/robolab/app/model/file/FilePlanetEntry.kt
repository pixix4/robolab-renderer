package de.robolab.app.model.file

import de.robolab.app.model.ISideBarGroup
import de.robolab.app.model.ISideBarPlottable
import de.robolab.app.model.ToolBarEntry
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
import de.westermann.kobserve.property.constProperty
import de.westermann.kobserve.property.property

class FilePlanetEntry(val filename: String, private val provider: FilePlanetProvider) : ISideBarPlottable {

    internal val planetFile = PlanetFile("")

    override val enabledProperty = property(false)

    override val drawable = EditPlanetDrawable()

    override val toolBarLeft: List<List<ToolBarEntry>> = listOf(
            listOf(
                    ToolBarEntry(constProperty("View"), selectedProperty = !drawable.editableProperty) {
                        drawable.editableProperty.value = false
                    },
                    ToolBarEntry(constProperty("Edit"), selectedProperty = drawable.editableProperty) {
                        drawable.editableProperty.value = true
                    }
            ),
            listOf(
                    ToolBarEntry(constProperty("Export as"), enabledProperty = constProperty(false)) {},
                    ToolBarEntry(constProperty("SVG")) {
                        var name = planetFile.planet.name.trim()
                        if (name.isEmpty()) {
                            name = "export"
                        }
                        saveExportSVG(name, exportSVG())
                    },
                    ToolBarEntry(constProperty("PNG")) {
                        var name = planetFile.planet.name.trim()
                        if (name.isEmpty()) {
                            name = "export"
                        }
                        saveExportPNG(name, exportPNG())
                    }
            )
    )

    override val toolBarRight: List<List<ToolBarEntry>> = listOf(
            listOf(
                    ToolBarEntry(iconProperty = constProperty(ToolBarEntry.Icon.UNDO), toolTipProperty = constProperty("Undo last action"), enabledProperty = planetFile.history.canUndoProperty) {
                        planetFile.history.undo()
                    },
                    ToolBarEntry(iconProperty = constProperty(ToolBarEntry.Icon.REDO), toolTipProperty = constProperty("Redo last action"), enabledProperty = planetFile.history.canRedoProperty) {
                        planetFile.history.redo()
                    }
            )
    )

    override val infoBarList = listOf(InfoBarFileEditor(this), InfoBarTraverser(this))
    override val selectedInfoBarIndexProperty = property<Int?>(0)

    val content: String
        get() = planetFile.content

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
