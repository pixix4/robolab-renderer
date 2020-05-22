package de.robolab.client.app.model.file

import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.web.adapter.WebCanvas
import de.robolab.client.web.dialog.Dialog
import de.robolab.client.web.dialog.ExportDialog
import de.robolab.client.web.dialog.PaperConstraintsDialog
import de.robolab.client.web.triggerDownload
import de.robolab.client.web.triggerDownloadPNG
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
    Dialog.open(ExportDialog(provider))
}

actual fun openPaperConstraintsDialog() {
    Dialog.open(PaperConstraintsDialog())
}