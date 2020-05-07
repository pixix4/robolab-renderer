package de.robolab.app.controller

import de.robolab.app.model.ISideBarPlottable
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
import kotlin.math.roundToInt

class ToolBarController(
        val selectedEntryProperty: ObservableProperty<ISideBarPlottable?>,
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
}
