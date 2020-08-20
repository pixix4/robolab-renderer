package de.robolab.client.app.model.file

import de.robolab.client.app.model.base.*
import de.robolab.client.app.model.file.provider.FilePlanet
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.canvas.SvgCanvas
import de.robolab.client.renderer.drawable.general.PointAnimatableManager
import de.robolab.client.renderer.drawable.planet.AbsPlanetDrawable
import de.robolab.client.renderer.drawable.planet.EditPlanetDrawable
import de.robolab.client.renderer.drawable.planet.SimplePlanetDrawable
import de.robolab.client.renderer.plotter.PlotterWindow
import de.robolab.client.renderer.utils.Transformation
import de.robolab.client.theme.LightTheme
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.utils.menuBilder
import de.robolab.common.parser.PlanetFile
import de.robolab.common.planet.Path
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Point
import de.robolab.common.utils.Rectangle
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.not
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FilePlanetEntry(
    val filePlanet: FilePlanet<*>
) : INavigationBarPlottable {

    val planetFile = filePlanet.planetFile

    override val enabledProperty = filePlanet.isLoadedProperty

    val drawable = EditPlanetDrawable(planetFile)

    override val document = drawable.view

    override val toolBarLeft: List<List<ToolBarEntry>> = listOf(
        listOf(
            ToolBarEntry(
                constObservable("View"),
                selectedProperty = !drawable.editableProperty
            ) {
                drawable.editableProperty.value = false
            },
            ToolBarEntry(
                constObservable("Edit"),
                selectedProperty = drawable.editableProperty
            ) {
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
                iconProperty = constObservable(MaterialIcon.BUILD),
                toolTipProperty = constObservable("Configure paper constraints")
            ) {
                openPaperConstraintsDialog()
            },
            ToolBarEntry(
                iconProperty = constObservable(MaterialIcon.COMPARE),
                toolTipProperty = constObservable("Flip view horizontal"),
                selectedProperty = drawable.flipViewProperty.mapBinding { it == true },
                enabledProperty = drawable.flipViewProperty.mapBinding { it != null }
            ) {
                drawable.flip()
            }
        ),
        listOf(
            ToolBarEntry(
                constObservable("Transform"),
                toolTipProperty = constObservable("Apply planet transformations")
            ) {
                openPlanetTransformDialog(planetFile)
            }
        )
    )

    override val hasContextMenu by enabledProperty

    override fun buildContextMenu(position: Point) = menuBilder(position, "Planet ${planetFile.planet.name}") {
        action("Save") {
            GlobalScope.launch(Dispatchers.Main) {
                save()
            }
        }
        action("Copy") {
            GlobalScope.launch(Dispatchers.Main) {
                copy()
            }
        }
        action("Delete") {
            GlobalScope.launch(Dispatchers.Main) {
                delete()
            }
        }
    }

    override val toolBarRight: List<List<ToolBarEntry>> = listOf(
        listOf(
            ToolBarEntry(
                iconProperty = constObservable(MaterialIcon.UNDO),
                toolTipProperty = constObservable("Undo last action"),
                enabledProperty = planetFile.history.canUndoProperty
            ) {
                planetFile.history.undo()
            },
            ToolBarEntry(
                iconProperty = constObservable(MaterialIcon.REDO),
                toolTipProperty = constObservable("Redo last action"),
                enabledProperty = planetFile.history.canRedoProperty
            ) {
                planetFile.history.redo()
            }
        )
    )

    override val infoBarList = listOf(InfoBarFileEditor(this), InfoBarTraverser(this))
    override val selectedInfoBarIndexProperty = property<Int?>(0)

    private val statisticsDetailBox = PlanetStatisticsDetailBox(planetFile)
    override val detailBoxProperty: ObservableValue<IDetailBox> = drawable.focusedElementsProperty.mapBinding { list ->
        when (val first = list.firstOrNull()) {
            is PointAnimatableManager.AttributePoint -> PointDetailBox(first, planetFile)
            is Path -> PathDetailBox(first, planetFile)
            else -> statisticsDetailBox
        }
    }

    val content: String
        get() = planetFile.contentString


    suspend fun save() {
        filePlanet.save()
    }

    suspend fun copy() {
        filePlanet.copy()
    }

    suspend fun delete() {
        filePlanet.delete()
    }

    suspend fun load() {
        filePlanet.load()
    }

    fun exportAsSVG(name: String = "") {
        var fileName = name
        if (fileName.isEmpty()) {
            fileName = planetFile.planet.name.trim()
        }
        if (fileName.isEmpty()) {
            fileName = "export"
        }
        saveExportSVG(fileName, writeToSVGString())
    }

    fun exportAsPNG(name: String = "") {
        var fileName = name
        if (fileName.isEmpty()) {
            fileName = planetFile.planet.name.trim()
        }
        if (fileName.isEmpty()) {
            fileName = "export"
        }
        saveExportPNG(fileName, drawToPNGCanvas())
    }

    private fun writeToSVGString(): String {
        val dimension = getExportSize()
        val canvas = SvgCanvas(dimension)

        exportRender(canvas)

        return canvas.buildFile()
    }

    private fun drawToPNGCanvas(): ICanvas {
        val canvas = exportPNGCanvas(getExportSize())

        exportRender(canvas)

        return canvas
    }

    private fun getExportSize(): Dimension {
        val rect = AbsPlanetDrawable.calcPlanetArea(planetFile.planet)?.expand(0.99) ?: Rectangle.ZERO
        return Dimension(rect.width * Transformation.PIXEL_PER_UNIT, rect.height * Transformation.PIXEL_PER_UNIT)
    }

    private fun exportRender(canvas: ICanvas) {
        val drawable = SimplePlanetDrawable()
        drawable.drawCompass = false
        drawable.drawName = true
        drawable.importPlanet(planetFile.planet)

        val plotter = PlotterWindow(canvas, drawable.view, LightTheme, 0.0)

        drawable.centerPlanet()

        plotter.render(0.0)
    }

    override val titleProperty = property(enabledProperty, planetFile.planetProperty) {
        if (enabledProperty.value) {
            val name = planetFile.planet.name
            if (name.isBlank()) "[Unnamed]" else name
        } else {
            filePlanet.localIdentifier?.name ?: filePlanet.remoteIdentifier?.name ?: "[Unnamed]"
        }
    }

    override val subtitleProperty = property(enabledProperty, planetFile.planetProperty) {
        if (enabledProperty.value) "Contains ${planetFile.planet.pathList.size} paths" else "Click to load"
    }

    override val tabNameProperty = titleProperty

    override val parent: INavigationBarGroup? = null

    override val statusIconProperty = filePlanet.hasLocalChangesProperty.mapBinding { hasLocalChanges ->
        if (hasLocalChanges) {
            listOf(MaterialIcon.SAVE)
        } else {
            emptyList()
        }
    }

    init {
        planetFile.history.onChange {
            drawable.importPlanet(planetFile.planet)
        }

        document.onAttach {
            GlobalScope.launch(Dispatchers.Main) {
                load()
            }
        }
    }
}

expect fun exportPNGCanvas(dimension: Dimension): ICanvas
expect fun saveExportSVG(name: String, content: String)
expect fun saveExportPNG(name: String, canvas: ICanvas)
expect fun openExportDialog(provider: FilePlanetEntry)
expect fun openPlanetTransformDialog(planetFile: PlanetFile)
expect fun openPaperConstraintsDialog()
expect fun openSendMessageDialog(topic: String, sendMessage: (String, String) -> Boolean)
