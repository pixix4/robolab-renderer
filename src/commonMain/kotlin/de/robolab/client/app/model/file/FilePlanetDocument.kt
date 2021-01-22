package de.robolab.client.app.model.file

import de.robolab.client.app.controller.InfoBarController
import de.robolab.client.app.controller.SendMessageController
import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.base.ToolBarEntry
import de.robolab.client.app.model.file.details.InfoBarFileEdit
import de.robolab.client.app.model.file.details.InfoBarFilePaper
import de.robolab.client.app.model.file.details.InfoBarFileTraverse
import de.robolab.client.app.model.file.details.InfoBarFileView
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

class FilePlanetDocument(
    val filePlanet: FilePlanet
) : IPlanetDocument {

    val planetFile = filePlanet.planetFile

    private val transformationStateProperty = property(Transformation.State.DEFAULT)
    private val viewDrawable = SimplePlanetDrawable(transformationStateProperty)
    private val paperDrawable = PaperPlanetDrawable(transformationStateProperty)
    val editDrawable = EditPlanetDrawable(planetFile, transformationStateProperty)
    private val traverserDrawable = LivePlanetDrawable(transformationStateProperty)

    @Suppress("PrivatePropertyName")
    private val VIEW = object : InfoBarController.Tab {
        override val icon = MaterialIcon.INFO_OUTLINE
        override val tooltip = "View"
        override fun open() {
            mode = this
        }
    }

    @Suppress("PrivatePropertyName")
    private val PAPER = object : InfoBarController.Tab {
        override val icon = MaterialIcon.SQUARE_FOOT
        override val tooltip = "Paper"
        override fun open() {
            mode = this
        }
    }

    @Suppress("PrivatePropertyName")
    private val EDIT = object : InfoBarController.Tab {
        override val icon = MaterialIcon.CODE
        override val tooltip = "Edit"
        override fun open() {
            mode = this
        }
    }

    @Suppress("PrivatePropertyName")
    private val TRAVERSE = object : InfoBarController.Tab {
        override val icon = MaterialIcon.CALL_SPLIT
        override val tooltip = "Traverse"
        override fun open() {
            mode = this
        }
    }

    private val modeProperty = property<InfoBarController.Tab>(VIEW)
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
                flippedProperty.value = !flippedProperty.value
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

    fun exportAsSVG(name: String = ""): Boolean {
        var fileName = name
        if (fileName.isEmpty()) {
            fileName = planetFile.planet.name.trim()
        }
        if (fileName.isEmpty()) {
            fileName = "export"
        }
        return saveExportSVG(fileName, writeToSVGString())
    }

    fun exportAsPNG(name: String = ""): Boolean {
        var fileName = name
        if (fileName.isEmpty()) {
            fileName = planetFile.planet.name.trim()
        }
        if (fileName.isEmpty()) {
            fileName = "export"
        }
        return saveExportPNG(fileName, drawToPNGCanvas())
    }

    fun exportAsExtendedPlanetFile(name: String = ""): Boolean {
        var fileName = name
        if (fileName.isEmpty()) {
            fileName = planetFile.planet.name.trim()
        }
        if (fileName.isEmpty()) {
            fileName = "export"
        }
        return saveExportExtendedPlanetFile(fileName, planetFile.extendedContentString())
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
            viewDrawable.importPlanet(planetFile.planet)
            paperDrawable.importPlanet(planetFile.planet)
            editDrawable.importPlanet(planetFile.planet)
            traverserDrawable.importBackgroundPlanet(planetFile.planet)
        }

        flippedProperty.onChange {
            viewDrawable.flip(flippedProperty.value)
            paperDrawable.flip(flippedProperty.value)
            editDrawable.flip(flippedProperty.value)
            traverserDrawable.flip(flippedProperty.value)
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
