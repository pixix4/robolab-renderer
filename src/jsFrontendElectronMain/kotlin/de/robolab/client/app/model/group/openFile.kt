package de.robolab.client.app.model.group

import de.robolab.client.app.controller.FileImportController
import de.robolab.client.ui.lineSequence
import de.robolab.client.ui.pathOrName
import kotlinx.datetime.Instant

actual suspend fun openFile(
    supportedFiles: List<Pair<String, List<String>>>,
    supportedFileTypes: List<String>,
): List<FileImportController.File> {
    val result = de.robolab.client.ui.openFile(*supportedFileTypes.toTypedArray())

    return result.map {
        FileImportController.File(
            it.pathOrName(),
            Instant.fromEpochMilliseconds("${it.lastModified}".toLong())
        ) {
            it.lineSequence()
        }
    }
}
