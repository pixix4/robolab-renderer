package de.robolab.client.app.controller

import de.robolab.client.app.controller.ui.ContentController
import de.robolab.client.app.controller.ui.UiController
import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.app.model.file.provider.RemoteMetadata
import de.robolab.client.app.model.file.provider.TempFilePlanetLoader
import de.robolab.client.communication.RobolabMessageProvider
import de.robolab.common.planet.PlanetFile
import de.robolab.common.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

class FileImportController(
    private val robolabMessageProvider: RobolabMessageProvider,
    private val filePlanetController: FilePlanetController,
    private val contentController: ContentController,
    private val uiController: UiController,
) {

    val logger = Logger(this)

    val supportedFileTypes = listOf(
        ".json",
        ".log"
    )

    val supportedFiles = listOf(
        "Supported files" to supportedFileTypes.map { "*$it" },
        "Planet file" to listOf("*.json"),
        "MQTT-Log file" to listOf("*.log"),
        "Any" to listOf("*")
    )

    fun isFileSupported(fileName: String): Boolean {
        return supportedFileTypes.any { fileName.endsWith(it) }
    }

    suspend fun importFile(fileName: String, lastModified: Instant, contentProvider: suspend () -> Sequence<String>) {
        return importFile(File(fileName, lastModified, contentProvider))
    }

    suspend fun importFile(file: File) {
        if (!isFileSupported(file.name)) return

        withContext(Dispatchers.Default) {
            try {
                if (file.name.endsWith(".json")) {
                    val content = file.contentProvider().joinToString("\n")
                    val planet = PlanetFile(content).planet
                    val metadata = RemoteMetadata.Planet(
                        planet.name,
                        file.lastModified,
                        planet.tags,
                        planet.getPointList().size
                    )
                    TempFilePlanetLoader.create(file.name, metadata, planet)
                    val filePlanet = filePlanetController.getFilePlanet(TempFilePlanetLoader, file.name)
                    contentController.openDocument(
                        FilePlanetDocument(filePlanet, uiController),
                        true
                    )
                } else if (file.name.endsWith(".log")) {
                    robolabMessageProvider.importMqttLog(file.contentProvider())
                }
            } catch (e: Exception) {
                logger.warn { e }
            }
        }
    }

    data class File(
        val name: String,
        val lastModified: Instant,
        val contentProvider: suspend () -> Sequence<String>,
    )
}
