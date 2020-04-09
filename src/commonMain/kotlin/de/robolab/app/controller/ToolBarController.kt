package de.robolab.app.controller

import de.robolab.app.model.ISideBarPlottable
import de.westermann.kobserve.Property
import de.westermann.kobserve.property.flatMapReadOnlyNullableBinding
import de.westermann.kobserve.property.mapBinding
import kotlin.math.roundToInt

class ToolBarController(
        val selectedEntryProperty: Property<ISideBarPlottable?>,
        private val canvasController: CanvasController
) {
    val actionListProperty = selectedEntryProperty.mapBinding { it?.actionList ?: emptyList() }

    val titleProperty = selectedEntryProperty.flatMapReadOnlyNullableBinding { it?.tabNameProperty }.mapBinding { it ?: "" }
    
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
