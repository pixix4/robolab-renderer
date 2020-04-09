package de.robolab.app.controller

import de.robolab.app.model.ISideBarPlottable
import de.robolab.app.model.file.toFixed
import de.robolab.planet.Coordinate
import de.robolab.planet.Path
import de.robolab.renderer.DefaultPlotter
import de.robolab.renderer.TransformationInteraction
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.BlankDrawable
import de.robolab.renderer.drawable.edit.*
import de.robolab.renderer.platform.CommonTimer
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.utils.Pointer
import de.robolab.utils.PreferenceStorage
import de.westermann.kobserve.Property
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlin.math.*

class CanvasController(
        val selectedEntryProperty: Property<ISideBarPlottable?>
) {
    private val timer = CommonTimer(50.0)

    private lateinit var plotter: DefaultPlotter
    fun setupCanvas(canvas: ICanvas) {
        if (this::plotter.isInitialized) {
            throw IllegalStateException("Plotter is already initialized!")
        }

        plotter = DefaultPlotter(canvas, timer, animationTime = 1000.0)

        plotter.theme = PreferenceStorage.selectedThemeProperty.value.theme
        PreferenceStorage.selectedThemeProperty.onChange {
            plotter.theme = PreferenceStorage.selectedThemeProperty.value.theme
        }

        selectedEntryProperty.onChange {
            val plottable = selectedEntryProperty.value
            if (plottable == null) {
                plotter.drawable = BlankDrawable
            } else {
                plotter.drawable = plottable.drawable
            }
        }

        pointerProperty.bind(plotter.pointerProperty)
        zoomProperty.bind(plotter.transformation.scaleProperty)
    }

    val pointerProperty = property<Pointer?>(null)
    val pointerStringProperty = pointerProperty.mapBinding { pointer ->
        if (pointer == null) return@mapBinding emptyList<String>()

        val list = mutableListOf<String>()
        list.add("Pointer: ${format(pointer.roundedPosition)}")
        if (plotter.transformation.scale != 1.0) {
            list.add("Zoom: ${(plotter.transformation.scale * 100).roundToInt()}%")
        }
        if (plotter.transformation.rotation != 0.0) {
            list.add("Rotation: ${((plotter.transformation.rotation / PI * 180) % 360).roundToInt()}%")
        }
        list.addAll(pointer.objectsUnderPointer.map(this::format))
        list.filter { it.isNotBlank() }
    }

    private fun format(obj: Any): String = when (obj) {
        is Path -> "Path(${obj.source.x},${obj.source.y},${obj.sourceDirection.name.first()} -> ${obj.target.x},${obj.target.y},${obj.targetDirection.name.first()})"
        is Coordinate -> "Coordinate(${obj.x},${obj.y})"
        is Point -> "${obj.left.toFixed(2)},${obj.top.toFixed(2)}"
        is EditDrawEndDrawable.PointEnd -> "PointEnd(${obj.point.x},${obj.point.y} -> ${obj.direction.name.first()})"
        is EditControlPointsDrawable.ControlPoint -> "ControlPoint(index ${obj.point} of ${format(obj.path)})"
        is Menu, is MenuList, is MenuAction -> ""
        else -> obj.toString()
    }

    val zoomProperty = property(1.0)

    private fun zoomDirected(direction: Int) {
        val currentZoomLevel = zoomProperty.value
        var nearestZoomLevel = ZOOM_STEPS.minBy { abs(it - currentZoomLevel) } ?: 1.0
        var index = ZOOM_STEPS.indexOf(nearestZoomLevel)

        if (direction * currentZoomLevel > direction * nearestZoomLevel) {
            index += direction
            index = max(0, min(index, ZOOM_STEPS.lastIndex))
        }
        nearestZoomLevel = ZOOM_STEPS[index]

        if (nearestZoomLevel != currentZoomLevel) {
            plotter.transformation.scaleTo(nearestZoomLevel, plotter.size / 2, TransformationInteraction.ANIMATION_TIME)
        } else {
            index += direction
            index = max(0, min(index, ZOOM_STEPS.lastIndex))
            plotter.transformation.scaleTo(ZOOM_STEPS[index], plotter.size / 2, TransformationInteraction.ANIMATION_TIME)

        }
    }

    fun zoomIn() = zoomDirected(1)

    fun zoomOut() = zoomDirected(-1)

    fun resetZoom() {
        plotter.transformation.scaleTo(1.0, plotter.size / 2, TransformationInteraction.ANIMATION_TIME)
    }

    companion object {
        private val ZOOM_STEPS = listOf(0.1, 0.3, 0.5, 0.67, 0.8, 0.9, 1.0, 1.1, 1.2, 1.33, 1.5, 1.7, 2.0, 2.4, 3.0, 4.0)
    }
}
