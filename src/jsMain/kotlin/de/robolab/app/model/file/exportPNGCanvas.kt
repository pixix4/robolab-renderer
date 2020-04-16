package de.robolab.app.model.file

import de.robolab.renderer.data.Dimension
import de.robolab.renderer.platform.ICanvas
import de.robolab.utils.PreferenceStorage
import de.robolab.web.adapter.WebCanvas
import de.robolab.web.dialog.Dialog
import de.robolab.web.dialog.ExportDialog
import de.robolab.web.dialog.PaperConstraintsDialog
import de.robolab.web.triggerDownload
import de.robolab.web.triggerDownloadPNG
import de.westermann.kwebview.components.Canvas

actual fun exportPNGCanvas(dimension: Dimension): ICanvas {
    val exportCanvas = Canvas()
    exportCanvas.updateSize(dimension.width.toInt(), dimension.height.toInt(), PreferenceStorage.exportScale)

   return WebCanvas(exportCanvas)
}

actual fun saveExportSVG(name:String, content: String) {
    triggerDownload("$name.svg", content)
}

actual fun saveExportPNG(name:String, canvas: ICanvas) {
    val webCanvas = canvas as? WebCanvas ?: return

    triggerDownloadPNG("$name.png", webCanvas.canvas)
}

actual fun openExportDialog(provider: FilePlanetEntry) {
    Dialog.open(ExportDialog(provider))
}

actual fun openPaperConstraintsDialog() {
    Dialog.open(PaperConstraintsDialog())
}