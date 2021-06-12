package de.robolab.client.app.model.group

import de.robolab.client.app.controller.FileImportController

actual suspend fun openFile(
    supportedFiles: List<Pair<String, List<String>>>,
    supportedFileTypes: List<String>,
): List<FileImportController.File> {
    throw UnsupportedOperationException()
}
