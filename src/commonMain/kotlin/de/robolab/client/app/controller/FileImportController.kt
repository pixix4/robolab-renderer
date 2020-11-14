package de.robolab.client.app.controller

import de.robolab.client.communication.RobolabMessageProvider
import de.robolab.common.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FileImportController(private val robolabMessageProvider: RobolabMessageProvider) {

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

    suspend fun importFile(fileName: String, content: Sequence<String>) {
        if (!isFileSupported(fileName)) return

        withContext(Dispatchers.Default) {
            try {
                if (fileName.endsWith(".planet")) {
                    logger.info { "Import of *.planet files is currently not supported!" }
                } else if (fileName.endsWith(".log")) {
                    robolabMessageProvider.importMqttLog(content)
                }
            } catch (e: Exception) {
                logger.warn { e }
            }
        }
    }
}
