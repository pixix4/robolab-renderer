package de.robolab.client.app.controller.ui

import de.robolab.client.app.controller.DialogController
import de.robolab.client.app.controller.RemoteServerController
import de.robolab.client.app.controller.dialog.SettingsDialogController
import de.robolab.client.app.viewmodel.MainViewModel
import de.robolab.client.app.viewmodel.dialog.SettingsDialogViewModel
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
import kotlin.math.roundToInt

class ToolBarController(
    private val contentController: ContentController,
    private val remoteServerController: RemoteServerController,
    val uiController: UiController
) {

    private val activeDocumentProperty = contentController.activeTabProperty.flatMapBinding { it.documentProperty }

    val fullscreenProperty = uiController.fullscreenProperty
    val navigationBarEnabledProperty = uiController.navigationBarEnabledProperty
    val infoBarEnabledProperty = uiController.infoBarEnabledProperty

    fun openSettingsDialog() {
        val dialog = SettingsDialogViewModel(SettingsDialogController(remoteServerController))
        DialogController.open(dialog)
    }

    val leftActionListProperty = activeDocumentProperty
        .flatMapBinding { it.toolBarLeft }
    val rightActionListProperty = activeDocumentProperty
        .flatMapBinding { it.toolBarRight }

    val titleProperty = activeDocumentProperty.nullableFlatMapBinding { it.nameProperty }.mapBinding {
        it ?: ""
    }

    val zoomProperty = contentController.zoomProperty.mapBinding {
        "${((it ?: 1.0) * 100).roundToInt()}%"
    }

    fun zoomIn() {
        contentController.zoomIn()
    }

    fun zoomOut() {
        contentController.zoomOut()
    }

    fun resetZoom() {
        contentController.resetZoom()
    }

    fun splitHorizontal() {
        contentController.content.splitEntryHorizontal()
    }

    fun splitVertical() {
        contentController.content.splitEntryVertical()
    }

    fun setGridLayout(rowCount: Int, colCount: Int) {
        contentController.content.setGridLayout(rowCount, colCount)
    }

    val canUndoProperty = activeDocumentProperty
        .nullableFlatMapBinding { it.canUndoProperty }
        .mapBinding { it == true }

    fun undo() {
        activeDocumentProperty.value.undo()
    }

    val canRedoProperty = activeDocumentProperty
        .nullableFlatMapBinding { it.canRedoProperty }
        .mapBinding { it == true }

    fun redo() {
        activeDocumentProperty.value.redo()
    }
}
