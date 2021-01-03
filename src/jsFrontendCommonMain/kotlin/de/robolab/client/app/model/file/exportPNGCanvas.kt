package de.robolab.client.app.model.file

import de.robolab.client.app.controller.SendMessageController
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.ui.adapter.WebCanvas
import de.robolab.client.ui.dialog.*
import de.robolab.client.ui.triggerDownload
import de.robolab.client.ui.triggerDownloadPNG
import de.robolab.common.parser.PlanetFile
import de.robolab.common.utils.Dimension
import de.westermann.kwebview.components.Canvas

actual fun createPNGExportCanvas(dimension: Dimension): ICanvas {
    val exportCanvas = Canvas()
    exportCanvas.updateSize(dimension.width.toInt(), dimension.height.toInt(), PreferenceStorage.exportScale)

    return WebCanvas(exportCanvas)
}

actual fun saveExportSVG(name: String, content: String): Boolean {
    triggerDownload("$name.svg", content)
    return true
}

actual fun saveExportExtendedPlanetFile(name: String, content: String): Boolean {
    triggerDownload("$name.planet", content)
    return true
}

actual fun saveExportPNG(name: String, canvas: ICanvas): Boolean {
    val webCanvas = canvas as? WebCanvas ?: return false

    triggerDownloadPNG("$name.png", webCanvas.canvas)
    return true
}

actual fun openExportDialog(planetDocument: FilePlanetDocument) {
    ExportDialog.open(planetDocument)
}

actual fun openPlanetTransformDialog(planetFile: PlanetFile) {
    PlanetTransformDialog.open(planetFile)
}

actual fun openSendMessageDialog(controller: SendMessageController) {
    SendMessageDialog.open(controller)
}
