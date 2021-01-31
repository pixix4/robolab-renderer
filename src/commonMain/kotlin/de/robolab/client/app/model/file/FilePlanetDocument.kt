package de.robolab.client.app.model.file

import de.robolab.client.app.controller.InfoBarController
import de.robolab.client.app.controller.SendMessageController
import de.robolab.client.app.model.base.IInfoBarContent
import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.base.ToolBarEntry
import de.robolab.client.app.model.file.details.*
import de.robolab.client.app.model.file.provider.FilePlanet
import de.robolab.client.renderer.Exporter
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.canvas.SvgCanvas
import de.robolab.client.renderer.drawable.planet.*
import de.robolab.client.renderer.utils.Transformation
import de.robolab.common.parser.PlanetFile
import de.robolab.common.planet.Planet
import de.robolab.common.utils.Dimension
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import de.westermann.kobserve.toggle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FilePlanetDocument(
    val filePlanet: FilePlanet
) : IPlanetDocument {

    val planetFile = filePlanet.planetFile

    private val transformationStateProperty = property(Transformation.State.DEFAULT)

    private val viewDrawable = SimplePlanetDrawable(transformationStateProperty)
    private val paperDrawable = PaperPlanetDrawable(transformationStateProperty)
    private val editDrawable = EditPlanetDrawable(planetFile, transformationStateProperty)
    private val traverserDrawable = LivePlanetDrawable(transformationStateProperty)
    private val testDrawable = SimplePlanetDrawable(transformationStateProperty)

    private val infoBarFileView = InfoBarFileView(this, viewDrawable)
    private val infoBarFilePaper = InfoBarFilePaper(this, paperDrawable)
    private val infoBarFileEdit = InfoBarFileEdit(this, editDrawable)
    private val infoBarFileTraverse = InfoBarFileTraverse(this)
    private val infoBarFileTest = InfoBarFileTest(this, testDrawable)

    inner class FilePlanetTab<T : AbsPlanetDrawable>(
        override val icon: MaterialIcon,
        override val tooltip: String,
        val drawable: T,
        val infoBarContent: IInfoBarContent,
        private val importer: FilePlanetTab<T>.(planet: Planet) -> Unit
    ) : InfoBarController.Tab {
        override fun open() {
            mode = this
        }

        fun importPlanet(planet: Planet) {
            importer(planet)
        }
    }

    override val infoBarTabsProperty = constObservable(listOf(
        FilePlanetTab(
            MaterialIcon.INFO_OUTLINE,
            "View",
            viewDrawable,
            infoBarFileView
        ) { planet ->
            drawable.importPlanet(planet)
        },
        FilePlanetTab(
            MaterialIcon.SQUARE_FOOT,
            "Paper",
            paperDrawable,
            infoBarFilePaper
        ) { planet ->
            drawable.importPlanet(planet)
        },
        FilePlanetTab(
            MaterialIcon.CODE,
            "Edit",
            editDrawable,
            infoBarFileEdit
        ) { planet ->
            drawable.importPlanet(planet)
        },
        FilePlanetTab(
            MaterialIcon.CALL_SPLIT,
            "Traverse",
            traverserDrawable,
            infoBarFileTraverse
        ) { planet ->
            drawable.importBackgroundPlanet(planet)
        },
        FilePlanetTab(
            MaterialIcon.BUG_REPORT,
            "Test suite",
            testDrawable,
            infoBarFileTest
        ) { planet ->
            drawable.importPlanet(planet)
        }
    ))
    private val infoBarTabs by infoBarTabsProperty

    private val modeProperty = property(infoBarTabs.first())
    var mode by modeProperty

    override val documentProperty = modeProperty.mapBinding {
        it.drawable.view
    }

    private val flippedProperty = property(false)

    override val toolBarLeft = constObservable(listOf(
        listOf(
            ToolBarEntry(
                iconProperty = constObservable(MaterialIcon.CAMERA_ALT),
                toolTipProperty = constObservable("Export dialog as …")
            ) {
                openExportDialog(this)
            },
        ),
        listOf(
            ToolBarEntry(
                iconProperty = constObservable(MaterialIcon.FLIP_CAMERA_ANDROID),
                toolTipProperty = constObservable("Flip planet view horizontally"),
                selectedProperty = flippedProperty
            ) {
                flippedProperty.toggle()
            }
        )
    ))

    override val toolBarRight = constObservable(listOf(
        listOf(
            ToolBarEntry(
                iconProperty = constObservable(MaterialIcon.SAVE),
                toolTipProperty = constObservable("Save changes"),
                enabledProperty = planetFile.history.canUndoProperty
            ) {
                GlobalScope.launch(Dispatchers.Main) {
                    save()
                }
            }
        )
    ))

    override val canUndoProperty = planetFile.history.canUndoProperty

    override fun undo() {
        planetFile.history.undo()
    }

    override val canRedoProperty = planetFile.history.canRedoProperty

    override fun redo() {
        planetFile.history.redo()
    }

    override val infoBarProperty = modeProperty.mapBinding {
        it.infoBarContent
    }

    override val infoBarActiveTabProperty = modeProperty

    val content: String
        get() = planetFile.contentString

    suspend fun save() {
        filePlanet.save()
    }

    suspend fun load() {
        filePlanet.load()
    }

    fun exportAsSVG(filename: String? = null): Boolean {
        return saveExportSVG(
            filename ?: Exporter.getExportName(planetFile.planet, "svg"),
            writeToSVGString()
        )
    }

    fun exportAsPNG(filename: String? = null): Boolean {
        return saveExportPNG(
            filename ?: Exporter.getExportName(planetFile.planet, "png"),
            drawToPNGCanvas()
        )
    }

    fun exportAsExtendedPlanetFile(filename: String? = null): Boolean {
        return saveExportExtendedPlanetFile(
            filename ?: Exporter.getExportName(planetFile.planet, "planet"),
            planetFile.extendedContentString()
        )
    }

    private fun writeToSVGString(): String {
        val dimension = Exporter.getDimension(planetFile.planet)
        val canvas = SvgCanvas(dimension)

        Exporter.renderToCanvas(planetFile.planet, canvas)

        return canvas.buildFile()
    }

    private fun drawToPNGCanvas(): ICanvas {
        val canvas = createPNGExportCanvas(Exporter.getDimension(planetFile.planet))

        Exporter.renderToCanvas(planetFile.planet, canvas)

        return canvas
    }

    override val nameProperty = planetFile.planetProperty.mapBinding { planet ->
        val name = planet.name
        if (name.isBlank()) "[Unnamed]" else name
    }

    override fun onCreate() {
        GlobalScope.launch(Dispatchers.Main) {
            load()
        }
    }

    override fun onAttach() {
    }

    override fun onDetach() {
    }

    override fun onDestroy() {
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FilePlanetDocument) return false

        if (filePlanet != other.filePlanet) return false

        return true
    }

    override fun hashCode(): Int {
        return filePlanet.hashCode()
    }

    fun transform() {
        openPlanetTransformDialog(planetFile)
    }

    fun format(explicit: Boolean) {
        planetFile.format(explicit)
    }

    init {
        planetFile.history.onChange {
            for (tab in infoBarTabs) {
                tab.importPlanet(planetFile.planet)
            }
        }

        flippedProperty.onChange {
            for (tab in infoBarTabs) {
                tab.drawable.flip(flippedProperty.value)
            }
        }

        infoBarFileTraverse.traverserRenderStateProperty.onChange {
            val state = infoBarFileTraverse.traverserRenderStateProperty.value

            traverserDrawable.importRobot(state?.robotDrawable)
            traverserDrawable.importServerPlanet(
                state?.planet?.importSenderGroups(
                    planetFile.planet, state.trail.locations
                )?.generateMissingSenderGroupings() ?: Planet.EMPTY
            )
        }
    }
}

expect fun createPNGExportCanvas(dimension: Dimension): ICanvas
expect fun saveExportPNG(name: String, canvas: ICanvas): Boolean
expect fun saveExportSVG(name: String, content: String): Boolean
expect fun saveExportExtendedPlanetFile(name: String, content: String): Boolean
expect fun openExportDialog(planetDocument: FilePlanetDocument)
expect fun openPlanetTransformDialog(planetFile: PlanetFile)
expect fun openSendMessageDialog(controller: SendMessageController)
