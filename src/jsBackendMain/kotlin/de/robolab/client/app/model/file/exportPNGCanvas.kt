package de.robolab.client.app.model.file


import de.robolab.client.app.controller.dialog.SendMessageDialogController
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.common.parser.PlanetFile
import de.robolab.common.utils.Dimension

actual fun createPNGExportCanvas(dimension: Dimension): ICanvas {
    throw UnsupportedOperationException()
}

actual fun saveExportSVG(name: String, content: String): Boolean {
    throw UnsupportedOperationException()
}

actual fun saveExportPNG(name: String, canvas: ICanvas): Boolean {
    throw UnsupportedOperationException()
}

actual fun saveExportExtendedPlanetFile(name: String, content: String): Boolean {
    throw UnsupportedOperationException()
}

actual fun openExportDialog(planetDocument: FilePlanetDocument) {
    throw UnsupportedOperationException()
}

actual fun openPlanetTransformDialog(planetFile: PlanetFile) {
    throw UnsupportedOperationException()
}

actual fun openSendMessageDialog(controller: SendMessageDialogController) {
    throw UnsupportedOperationException()
}