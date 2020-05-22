package de.robolab.client.app.model.file

import de.robolab.client.jfx.adapter.AwtCanvas
import de.robolab.client.jfx.dialog.ExportDialog
import de.robolab.client.jfx.dialog.PaperConstraintsDialog
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.utils.Dimension
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
