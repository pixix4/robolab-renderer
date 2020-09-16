package de.robolab.client.app.model.file


import de.robolab.client.app.controller.SendMessageController
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.common.parser.PlanetFile
import de.robolab.common.utils.Dimension

actual fun createPNGExportCanvas(dimension: Dimension): ICanvas {
    throw UnsupportedOperationException()
}

actual fun saveExportSVG(name: String, content: String) {
    throw UnsupportedOperationException()
}

actual fun saveExportPNG(name: String, canvas: ICanvas) {
    throw UnsupportedOperationException()
}

actual fun openExportDialog(planetDocument: FileEntryPlanetDocument) {
    throw UnsupportedOperationException()
}

actual fun openPlanetTransformDialog(planetFile: PlanetFile) {
    throw UnsupportedOperationException()
}

actual fun openSendMessageDialog(controller: SendMessageController) {
    throw UnsupportedOperationException()
}