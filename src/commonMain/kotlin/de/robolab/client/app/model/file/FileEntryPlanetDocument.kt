package de.robolab.client.app.model.file

import de.robolab.client.app.controller.InfoBarController
import de.robolab.client.app.controller.SendMessageController
import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.base.ToolBarEntry
import de.robolab.client.app.model.file.provider.FilePlanet
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.canvas.SvgCanvas
import de.robolab.client.renderer.drawable.planet.*
import de.robolab.client.renderer.plotter.PlotterWindow
import de.robolab.client.renderer.utils.Transformation
import de.robolab.client.theme.LightTheme
import de.robolab.common.parser.PlanetFile
import de.robolab.common.planet.Planet
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Rectangle
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.exp

class FileEntryPlanetDocument(
    val filePlanet: FilePlanet<*>
) : IPlanetDocument {

    val planetFile = filePlanet.planetFile

    private val transformationStateProperty = property(Transformation.State.DEFAULT)
    val viewDrawable = SimplePlanetDrawable(transformationStateProperty)
    val paperDrawable = PaperPlanetDrawable(transformationStateProperty)
    val editDrawable = EditPlanetDrawable(planetFile, transformationStateProperty)
    val traverserDrawable = LivePlanetDrawable(transformationStateProperty)

    val VIEW = object : InfoBarController.Tab {
        override val icon = MaterialIcon.INFO_OUTLINE
        override val tooltip = "View"
        override fun open() {
            mode = this
        }
    }
    val PAPER = object : InfoBarController.Tab {
        override val icon = MaterialIcon.STRAIGHTEN
        override val tooltip = "Paper"
        override fun open() {
            mode = this
        }
    }
    val EDIT = object : InfoBarController.Tab {
        override val icon = MaterialIcon.CODE
        override val tooltip = "Edit"
        override fun open() {
            mode = this
        }
    }
    val TRAVERSE = object : InfoBarController.Tab {
        override val icon = MaterialIcon.CALL_SPLIT
        override val tooltip = "Traverse"
        override fun open() {
            mode = this
        }
    }

    val modeProperty = property(VIEW)
    var mode: InfoBarController.Tab by modeProperty

    override val documentProperty = modeProperty.mapBinding {
        when (mode) {
            VIEW -> viewDrawable.view
            PAPER -> paperDrawable.view
            EDIT -> editDrawable.view
            TRAVERSE -> traverserDrawable.view
            else -> throw IllegalStateException()
        }
    }

    override val toolBarLeft = constObservable(listOf(
        listOf(
            ToolBarEntry(
                constObservable("Export"),
                toolTipProperty = constObservable("Open planet export dialog")
            ) {
                openExportDialog(this)
            },
            ToolBarEntry(
                constObservable("Transform"),
                toolTipProperty = constObservable("Apply planet transformations")
            ) {
                openPlanetTransformDialog(planetFile)
            },
            ToolBarEntry(
                constObservable("Flip"),
                toolTipProperty = constObservable("Flip the planet")
            ) {
                viewDrawable.flip()
                paperDrawable.flip()
                editDrawable.flip()
                traverserDrawable.flip()
            }
        )
    ))

    override val toolBarRight = constObservable(listOf(
        listOf(
            ToolBarEntry(
                iconProperty = constObservable(MaterialIcon.SAVE),
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

    private val infoBarFileView = InfoBarFileView(this)
    private val infoBarFilePaper = InfoBarFilePaper(this)
    private val infoBarFileEdit = InfoBarFileEdit(this)
    private val infoBarFileTraverse = InfoBarFileTraverse(this)

    override val infoBarProperty = modeProperty.mapBinding {
        when (mode) {
            VIEW -> infoBarFileView
            PAPER -> infoBarFilePaper
            EDIT -> infoBarFileEdit
            TRAVERSE -> infoBarFileTraverse
            else -> null
        }
    }

    override val infoBarTabsProperty = constObservable(
        listOf(
            VIEW,
            PAPER,
            EDIT,
            TRAVERSE
        )
    )

    override val infoBarActiveTabProperty = modeProperty

    val content: String
        get() = planetFile.contentString

    suspend fun save() {
        filePlanet.save()
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
        val canvas = createPNGExportCanvas(getExportSize())

        exportRender(canvas)

        return canvas
    }

    private fun getExportSize(): Dimension {
        val rect = AbsPlanetDrawable.calcPlanetArea(planetFile.planet)?.expand(0.99) ?: Rectangle.ZERO
        return Dimension(rect.width * Transformation.PIXEL_PER_UNIT, rect.height * Transformation.PIXEL_PER_UNIT)
    }

    private fun exportRender(canvas: ICanvas) {
        val exportDocument = FileExportDocument(planetFile)
        val plotter = PlotterWindow(canvas, exportDocument, LightTheme, 0.0)

        exportDocument.center()

        plotter.render(0.0)
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
        if (other !is FileEntryPlanetDocument) return false

        if (filePlanet != other.filePlanet) return false

        return true
    }

    override fun hashCode(): Int {
        return filePlanet.hashCode()
    }

    init {
        planetFile.history.onChange {
            viewDrawable.importPlanet(planetFile.planet)
            paperDrawable.importPlanet(planetFile.planet)
            editDrawable.importPlanet(planetFile.planet)
            traverserDrawable.importBackgroundPlanet(planetFile.planet)
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
expect fun saveExportSVG(name: String, content: String)
expect fun saveExportPNG(name: String, canvas: ICanvas)
expect fun openExportDialog(planetDocument: FileEntryPlanetDocument)
expect fun openPlanetTransformDialog(planetFile: PlanetFile)
expect fun openSendMessageDialog(controller: SendMessageController)
