package de.robolab.client.app.controller

import com.soywiz.klock.DateTime
import de.robolab.client.app.controller.ui.ContentController
import de.robolab.client.app.controller.ui.UiController
import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.app.model.file.provider.FilePlanet
import de.robolab.client.app.model.file.provider.RemoteMetadata
import de.robolab.client.app.model.file.provider.TempFilePlanetLoader
import de.robolab.client.communication.RobolabMessageProvider
import de.robolab.common.parser.PlanetFile
import de.robolab.common.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FileImportController(
    private val robolabMessageProvider: RobolabMessageProvider,
    private val filePlanetController: FilePlanetController,
    private val contentController: ContentController,
    private val uiController: UiController
) {

    val logger = Logger(this)

    val supportedFileTypes = listOf(
        ".planet",
        ".log"
    )

    val supportedFiles = listOf(
        "Supported files" to supportedFileTypes.map { "*$it" },
        "Planet file" to listOf("*.planet"),
        "MQTT-Log file" to listOf("*.log"),
        "Any" to listOf("*")
    )

    fun isFileSupported(fileName: String): Boolean {
        return supportedFileTypes.any { fileName.endsWith(it) }
    }

    suspend fun importFile(fileName: String, lastModified: DateTime, contentProvider: suspend () -> Sequence<String>) {
        if (!isFileSupported(fileName)) return

        withContext(Dispatchers.Default) {
            try {
                if (fileName.endsWith(".planet")) {
                    val content = contentProvider().toList()
                    val planet = PlanetFile(content).planet
                    val metadata = RemoteMetadata.Planet(
                        planet.name,
                        lastModified,
                        planet.tagMap,
                        planet.getPointList().size
                    )
                    TempFilePlanetLoader.create(fileName, metadata, content)
                    val filePlanet = filePlanetController.getFilePlanet(TempFilePlanetLoader, fileName)
                    contentController.openDocument(
                        FilePlanetDocument(filePlanet, uiController),
                        true
                    )
                    logger.info { "Import of *.planet files is currently not supported!" }
                } else if (fileName.endsWith(".log")) {
                    robolabMessageProvider.importMqttLog(contentProvider())
                }
            } catch (e: Exception) {
                logger.warn { e }
            }
        }
    }
}
