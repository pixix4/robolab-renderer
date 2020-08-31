package de.robolab.client.app.model.file

import de.robolab.client.app.controller.SendMessageController
import de.robolab.client.ui.adapter.AwtCanvas
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.parser.PlanetFile
import de.robolab.common.utils.Dimension
import java.io.File

actual fun exportPNGCanvas(dimension: Dimension): ICanvas {
    return AwtCanvas(dimension, PreferenceStorage.exportScale)
}

actual fun saveExportSVG(name: String, content: String) {
    File("$name.svg").writeText(content)
}

actual fun saveExportPNG(name: String, canvas: ICanvas) {
    val exportCanvas = canvas as? AwtCanvas ?: return
    exportCanvas.writePNG(File("$name.png"))
}

actual fun openExportDialog(provider: FilePlanetEntry) {
    throw UnsupportedOperationException()
}

actual fun openPlanetTransformDialog(planetFile: PlanetFile) {
    throw UnsupportedOperationException()
}

actual fun openSendMessageDialog(controller: SendMessageController) {
    throw UnsupportedOperationException()
}
