package de.robolab.client.app.model.file

import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.ui.adapter.WebCanvas
import de.robolab.client.ui.dialog.*
import de.robolab.client.ui.triggerDownload
import de.robolab.client.ui.triggerDownloadPNG
import de.robolab.common.parser.PlanetFile
import de.robolab.common.utils.Dimension
import de.westermann.kwebview.components.Canvas

actual fun exportPNGCanvas(dimension: Dimension): ICanvas {
    val exportCanvas = Canvas()
    exportCanvas.updateSize(dimension.width.toInt(), dimension.height.toInt(), PreferenceStorage.exportScale)

    return WebCanvas(exportCanvas)
}

actual fun saveExportSVG(name: String, content: String) {
    triggerDownload("$name.svg", content)
}

actual fun saveExportPNG(name: String, canvas: ICanvas) {
    val webCanvas = canvas as? WebCanvas ?: return

    triggerDownloadPNG("$name.png", webCanvas.canvas)
}

actual fun openExportDialog(provider: FilePlanetEntry) {
    ExportDialog.open(provider)
}

actual fun openPaperConstraintsDialog() {
    PaperConstraintsDialog.open()
}

actual fun openPlanetTransformDialog(planetFile: PlanetFile) {
    PlanetTransformDialog.open(planetFile)
}

actual fun openSendMessageDialog(topic: String, sendMessage: (String, String) -> Boolean) {
    SendMessageDialog.open(topic, sendMessage)
}
