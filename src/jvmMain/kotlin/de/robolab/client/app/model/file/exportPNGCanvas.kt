package de.robolab.client.app.model.file

import de.robolab.client.app.controller.SendMessageController
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.ui.adapter.AwtCanvas
import de.robolab.client.ui.dialog.ExportDialog
import de.robolab.client.ui.dialog.PlanetTransformDialog
import de.robolab.client.ui.dialog.SendMessageDialog
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.parser.PlanetFile
import de.robolab.common.utils.Dimension
import javafx.stage.FileChooser
import javafx.stage.Stage
import tornadofx.FileChooserMode
import tornadofx.chooseFile
import java.io.File

actual fun createPNGExportCanvas(dimension: Dimension): ICanvas {
    return AwtCanvas(dimension, PreferenceStorage.exportScale)
}

actual fun saveExportSVG(name: String, content: String): Boolean {
    val file = if (Stage.getWindows().isNotEmpty()) {
        chooseFile(
            "Export planet as svg",
            arrayOf(FileChooser.ExtensionFilter("SVG File", "*.svg")),
            null,
            "$name.svg",
            mode = FileChooserMode.Save
        ).firstOrNull() ?: return false
    } else {
        File("$name.svg")
    }
    file.writeText(content)
    return true
}

actual fun saveExportPNG(name: String, canvas: ICanvas): Boolean {
    val exportCanvas = canvas as? AwtCanvas ?: return false

    val file = if (Stage.getWindows().isNotEmpty()) {
        chooseFile(
            "Export planet as png",
            arrayOf(FileChooser.ExtensionFilter("PNG File", "*.png")),
            null,
            "$name.png",
            mode = FileChooserMode.Save
        ).firstOrNull() ?: return false
    } else {
        File("$name.png")
    }
    exportCanvas.writePNG(file)
    return true
}

actual fun openExportDialog(planetDocument: FileEntryPlanetDocument) {
    ExportDialog.open(planetDocument)
}

actual fun openPlanetTransformDialog(planetFile: PlanetFile) {
    PlanetTransformDialog.open(planetFile)
}

actual fun openSendMessageDialog(controller: SendMessageController) {
    SendMessageDialog.open(controller)
}
