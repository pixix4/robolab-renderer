package de.robolab.app.model.file

import de.robolab.jfx.adapter.AwtCanvas
import de.robolab.jfx.dialog.ExportDialog
import de.robolab.jfx.dialog.PaperConstraintsDialog
import de.robolab.renderer.data.Dimension
import de.robolab.renderer.platform.ICanvas
import de.robolab.utils.PreferenceStorage
import javafx.stage.StageStyle
import tornadofx.*
import java.io.File

actual fun exportPNGCanvas(dimension: Dimension): ICanvas {
    return AwtCanvas(dimension.width, dimension.height, PreferenceStorage.exportScale)
}

actual fun saveExportSVG(name: String, content: String) {
    File("$name.svg").writeText(content)
}

actual fun saveExportPNG(name: String, canvas: ICanvas) {
    val exportCanvas = canvas as? AwtCanvas ?: return
    exportCanvas.writePNG(File("$name.png"))
}

actual fun openExportDialog(provider: FilePlanetEntry) {
    ExportDialog.open(provider)
}

actual fun openPaperConstraintsDialog() {
    PaperConstraintsDialog.open()
}
