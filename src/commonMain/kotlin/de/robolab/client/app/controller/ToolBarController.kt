package de.robolab.client.app.controller

import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.app.model.file.FileNavigationRoot
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
import de.westermann.kobserve.property.property
import kotlin.math.roundToInt

class ToolBarController(
    private val activeTabProperty: ObservableValue<TabController.Tab?>,
    private val activeDocumentProperty: ObservableValue<IPlanetDocument?>,
    private val canvasController: CanvasController,
    val uiController: UiController,
    val fileNavigationRoot: FileNavigationRoot
) {

    val fullscreenProperty = uiController.fullscreenProperty

    fun toggleFullscreen() {
        fullscreenProperty.value = !fullscreenProperty.value
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
