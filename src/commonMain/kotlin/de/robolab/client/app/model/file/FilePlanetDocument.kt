package de.robolab.client.app.model.file

import de.robolab.client.app.controller.DialogController
import de.robolab.client.app.controller.ui.UiController
import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.details.*
import de.robolab.client.app.model.file.provider.FilePlanet
import de.robolab.client.app.viewmodel.FormContentViewModel
import de.robolab.client.app.viewmodel.SideBarTabViewModel
import de.robolab.client.app.viewmodel.dialog.ExportPlanetDialogViewModel
import de.robolab.client.app.viewmodel.dialog.TransformPlanetDialogViewModel
import de.robolab.client.renderer.Exporter
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.canvas.SvgCanvas
import de.robolab.client.renderer.drawable.planet.AbsPlanetDrawable
import de.robolab.client.renderer.drawable.planet.SimplePlanetDrawable
import de.robolab.client.renderer.utils.Transformation
import de.robolab.common.planet.Planet
import de.robolab.common.utils.Dimension
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FilePlanetDocument(
    val filePlanet: FilePlanet,
    uiController: UiController
) : IPlanetDocument {

    val planetFile = filePlanet.planetFile

    val transformationStateProperty = property(Transformation.State.DEFAULT)

    abstract class FilePlanetSideBarTab<T : AbsPlanetDrawable>(
        name: String,
        icon: MaterialIcon
    ) : SideBarTabViewModel(name, icon) {

        abstract val drawable: T

        abstract fun importPlanet(planet: Planet)
    }

    override val infoBarTabs = listOf<FilePlanetSideBarTab<*>>(
        InfoBarFileView(this, uiController),
        InfoBarFilePaper(this, uiController),
        InfoBarFileEdit(this, uiController),
        InfoBarFileTraverse(this, uiController),
        InfoBarFileTest(this, uiController)

    )

    override val activeTabProperty = property<SideBarTabViewModel?>(infoBarTabs.first())

    override val documentProperty = activeTabProperty.mapBinding {
        val tab = it as? FilePlanetSideBarTab<*>
        tab?.drawable?.view ?: SimplePlanetDrawable().view
    }

    private val flippedProperty = property(false)

    override val toolBarLeft = constObservable(listOf(
        FormContentViewModel.Group(
            FormContentViewModel.Button(
                MaterialIcon.CAMERA_ALT,
                description = "Export dialog as …"
            ) {
                DialogController.open(ExportPlanetDialogViewModel(this))
            }
        ),
        FormContentViewModel.Group(
            FormContentViewModel.ToggleButton(
                flippedProperty,
                MaterialIcon.FLIP_CAMERA_ANDROID,
                description = "Flip planet view horizontally"
            )
        ),
    ))

    override val toolBarRight = constObservable(listOf(
        FormContentViewModel.Group(
            FormContentViewModel.Button(
                MaterialIcon.SAVE,
                description = "Save changes",
                enabledProperty = planetFile.history.canUndoProperty
            ) {
                GlobalScope.launch(Dispatchers.Main) {
                    save()
                }
            }
        ),
    ))

    override val canUndoProperty = planetFile.history.canUndoProperty

    override fun undo() {
        planetFile.history.undo()
    }

    override val canRedoProperty = planetFile.history.canRedoProperty

    override fun redo() {
        planetFile.history.redo()
    }

    val content: String
        get() = planetFile.contentString

    suspend fun save() {
        filePlanet.save()
    }

    suspend fun load() {
        filePlanet.update()
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
        DialogController.open(TransformPlanetDialogViewModel(planetFile))
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
    }
}

expect fun createPNGExportCanvas(dimension: Dimension): ICanvas
expect fun saveExportPNG(name: String, canvas: ICanvas): Boolean
expect fun saveExportSVG(name: String, content: String): Boolean
expect fun saveExportExtendedPlanetFile(name: String, content: String): Boolean
