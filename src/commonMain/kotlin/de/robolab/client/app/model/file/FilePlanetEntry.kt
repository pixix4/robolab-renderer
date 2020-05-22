package de.robolab.client.app.model.file

import de.robolab.client.app.model.ISideBarGroup
import de.robolab.client.app.model.ISideBarPlottable
import de.robolab.client.app.model.ToolBarEntry
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.canvas.SvgCanvas
import de.robolab.client.renderer.drawable.planet.AbsPlanetDrawable
import de.robolab.client.renderer.drawable.planet.EditPlanetDrawable
import de.robolab.client.renderer.drawable.planet.SimplePlanetDrawable
import de.robolab.client.renderer.plotter.ExportPlotter
import de.robolab.client.renderer.utils.Transformation
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.utils.menuBilder
import de.robolab.common.parser.PlanetFile
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Logger
import de.robolab.common.utils.Point
import de.robolab.common.utils.Rectangle
import de.westermann.kobserve.not
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class FilePlanetEntry(val filename: String, private val provider: FilePlanetProvider) : ISideBarPlottable {

    private val logger = Logger(this)

    internal val planetFile = PlanetFile("")

    override val enabledProperty = property(false)

    val drawable = EditPlanetDrawable(planetFile)

    override val document = drawable.view

    override val toolBarLeft: List<List<ToolBarEntry>> = listOf(
        listOf(
            ToolBarEntry(constObservable("View"), selectedProperty = !drawable.editableProperty) {
                drawable.editableProperty.value = false
            },
            ToolBarEntry(constObservable("Edit"), selectedProperty = drawable.editableProperty) {
                drawable.editableProperty.value = true
            }
        ),
        listOf(
            ToolBarEntry(
                constObservable("Export"),
                toolTipProperty = constObservable("Open planet export dialog")
            ) {
                openExportDialog(this)
            }
        ),
        listOf(
            ToolBarEntry(
                constObservable("Paper"),
                toolTipProperty = constObservable("Toggle paper constraints background"),
                selectedProperty = PreferenceStorage.paperBackgroundEnabledProperty
            ) {
                PreferenceStorage.paperBackgroundEnabledProperty.value =
                    !PreferenceStorage.paperBackgroundEnabledProperty.value
            },
            ToolBarEntry(
                iconProperty = constObservable(ToolBarEntry.Icon.PREFERENCES),
                toolTipProperty = constObservable("Configure paper constraints")
            ) {
                openPaperConstraintsDialog()
            },
            ToolBarEntry(
                iconProperty = constObservable(ToolBarEntry.Icon.FLIP),
                toolTipProperty = constObservable("Flip view horizontal"),
                selectedProperty = drawable.flipViewProperty.mapBinding { it == true },
                enabledProperty = drawable.flipViewProperty.mapBinding { it != null }
            ) {
                drawable.flip()
            }
        )
    )

    override val hasContextMenu by enabledProperty

    override fun buildContextMenu(position: Point) = menuBilder(position, "Planet ${planetFile.planet.name}") {
        action("Save") {
            provider.saveEntry(this@FilePlanetEntry) {
                logger.info { "Save successful: $it" }
            }
        }
    }

    override val toolBarRight: List<List<ToolBarEntry>> = listOf(
        listOf(
            ToolBarEntry(
                iconProperty = constObservable(ToolBarEntry.Icon.UNDO),
                toolTipProperty = constObservable("Undo last action"),
                enabledProperty = planetFile.history.canUndoProperty
            ) {
                planetFile.history.undo()
            },
            ToolBarEntry(
                iconProperty = constObservable(ToolBarEntry.Icon.REDO),
                toolTipProperty = constObservable("Redo last action"),
                enabledProperty = planetFile.history.canRedoProperty
            ) {
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


    fun exportAsSVG(name: String = "") {
        var fileName = name
        if (fileName.isEmpty()) {
            fileName = planetFile.planet.name.trim()
        }
        if (fileName.isEmpty()) {
            fileName = "export"
        }
        saveExportSVG(fileName, exportSVG())
    }

    fun exportAsPNG(name: String = "") {
        var fileName = name
        if (fileName.isEmpty()) {
            fileName = planetFile.planet.name.trim()
        }
        if (fileName.isEmpty()) {
            fileName = "export"
        }
        saveExportPNG(fileName, exportPNG())
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
        val rect = AbsPlanetDrawable.calcPlanetArea(planetFile.planet)?.expand(0.99) ?: Rectangle.ZERO
        return Dimension(rect.width * Transformation.PIXEL_PER_UNIT, rect.height * Transformation.PIXEL_PER_UNIT)
    }

    private fun exportRender(canvas: ICanvas) {
        val drawable = SimplePlanetDrawable()
        drawable.drawCompass = false
        drawable.drawName = true
        drawable.importPlanet(planetFile.planet)

        val plotter = ExportPlotter(canvas, drawable.view)

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
        planetFile.history.onChange {
            drawable.importPlanet(planetFile.planet)
        }
    }
}

expect fun exportPNGCanvas(dimension: Dimension): ICanvas
expect fun saveExportSVG(name: String, content: String)
expect fun saveExportPNG(name: String, canvas: ICanvas)
expect fun openExportDialog(provider: FilePlanetEntry)
expect fun openPaperConstraintsDialog()
