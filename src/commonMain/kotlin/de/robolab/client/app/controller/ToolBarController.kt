package de.robolab.client.app.controller

import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.app.model.file.FileNavigationManager
import de.robolab.client.net.requests.mqtt.GetMQTTURLs
import de.robolab.client.net.requests.mqtt.getMQTTCredentials
import de.robolab.client.net.requests.mqtt.getMQTTURLs
import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
import de.westermann.kobserve.toggle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class ToolBarController(
    private val activeTabProperty: ObservableValue<TabController.Tab?>,
    private val activeDocumentProperty: ObservableValue<IPlanetDocument?>,
    private val canvasController: CanvasController,
    val uiController: UiController,
    val fileNavigationManager: FileNavigationManager
) {

    val fullscreenProperty = uiController.fullscreenProperty

    fun toggleFullscreen() {
        fullscreenProperty.toggle()
    }

    fun requestAuthToken() {
        val server = fileNavigationManager.remoteServer
        if (server != null) {
            GlobalScope.launch {
                de.robolab.client.app.model.file.requestAuthToken(server, false)
            }
        }
    }

    fun loadMqttSettings(selectUri: (GetMQTTURLs.MQTTURLsResponse) -> String) {
        val server = fileNavigationManager.remoteServer
        if (server != null) {
            GlobalScope.launch {
                val credentials = server.getMQTTCredentials().okOrNull() ?: return@launch
                val urls = server.getMQTTURLs().okOrNull() ?: return@launch

                withContext(Dispatchers.Main) {
                    PreferenceStorage.serverUri = selectUri(urls)
                    PreferenceStorage.logUri = urls.logURL
                    PreferenceStorage.username = credentials.credentials.username
                    PreferenceStorage.password = credentials.credentials.password
                }
            }
        }
    }

    val leftActionListProperty = activeDocumentProperty
        .nullableFlatMapBinding { it?.toolBarLeft ?: constObservable(emptyList())  }
    val rightActionListProperty = activeDocumentProperty
        .nullableFlatMapBinding { it?.toolBarRight ?: constObservable(emptyList()) }

    val titleProperty = activeDocumentProperty.nullableFlatMapBinding { it?.nameProperty }.mapBinding {
        it ?: ""
    }

    val zoomProperty = canvasController.zoomProperty.mapBinding {
        "${((it ?: 1.0) * 100).roundToInt()}%"
    }

    fun zoomIn() {
        canvasController.zoomIn()
    }

    fun zoomOut() {
        canvasController.zoomOut()
    }

    fun resetZoom() {
        canvasController.resetZoom()
    }

    fun splitHorizontal() {
        activeTabProperty.value?.plotterManager?.splitHorizontal()
    }

    fun splitVertical() {
        activeTabProperty.value?.plotterManager?.splitVertical()
    }

    fun closeWindow() {
        activeTabProperty.value?.plotterManager?.closeWindow()
    }

    fun setGridLayout(rowCount: Int, colCount: Int) {
        activeTabProperty.value?.plotterManager?.setGridLayout(rowCount, colCount)
    }

    val canUndoProperty = activeDocumentProperty
        .nullableFlatMapBinding { it?.canUndoProperty }
        .mapBinding { it == true }

    fun undo() {
        activeDocumentProperty.value?.undo()
    }

    val canRedoProperty = activeDocumentProperty
        .nullableFlatMapBinding { it?.canRedoProperty }
        .mapBinding { it == true }

    fun redo() {
        activeDocumentProperty.value?.redo()
    }
}
