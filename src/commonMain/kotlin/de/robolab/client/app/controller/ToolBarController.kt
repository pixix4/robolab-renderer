package de.robolab.client.app.controller

import de.robolab.client.app.model.base.INavigationBarPlottable
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
import kotlin.math.roundToInt

class ToolBarController(
    selectedEntryProperty: ObservableProperty<INavigationBarPlottable?>,
    private val canvasController: CanvasController
) {
    val leftActionListProperty = selectedEntryProperty.mapBinding { it?.toolBarLeft ?: emptyList() }
    val rightActionListProperty = selectedEntryProperty.mapBinding { it?.toolBarRight ?: emptyList() }

    val titleProperty = selectedEntryProperty.nullableFlatMapBinding { it?.tabNameProperty }.mapBinding {
        it ?: ""
    }

    val zoomProperty = canvasController.zoomProperty.mapBinding {
        "${(it * 100).roundToInt()}%"
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
        canvasController.plotter.splitHorizontal()
    }

    fun splitVertical() {
        canvasController.plotter.splitVertical()
    }

    fun closeWindow() {
        canvasController.plotter.closeWindow()
    }

    fun setGridLayout(rowCount: Int, colCount: Int) {
        canvasController.plotter.setGridLayout(rowCount, colCount)
    }
}
