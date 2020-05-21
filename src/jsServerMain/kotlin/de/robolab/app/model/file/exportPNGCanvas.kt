package de.robolab.app.model.file


import de.robolab.renderer.data.Dimension
import de.robolab.renderer.platform.ICanvas

actual fun exportPNGCanvas(dimension: Dimension): ICanvas {
    throw UnsupportedOperationException()
}

actual fun saveExportSVG(name: String, content: String) {
    throw UnsupportedOperationException()
}

actual fun saveExportPNG(name: String, canvas: ICanvas) {
    throw UnsupportedOperationException()
}

actual fun openExportDialog(provider: FilePlanetEntry) {
    throw UnsupportedOperationException()
}

actual fun openPaperConstraintsDialog() {
    throw UnsupportedOperationException()
}
