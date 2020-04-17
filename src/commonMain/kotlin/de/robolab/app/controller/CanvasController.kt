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
        private val selectedEntryProperty: Property<ISideBarPlottable?>
) {
    private val timer = CommonTimer(50.0)

    private lateinit var plotter: DefaultPlotter
    fun setupCanvas(canvas: ICanvas) {
        if (this::plotter.isInitialized) {
            throw IllegalStateException("Plotter is already initialized!")
        }

        plotter = DefaultPlotter(canvas, timer, animationTime = 1000.0)

        selectedEntryProperty.onChange {
            val plottable = selectedEntryProperty.value
            if (plottable == null) {
                plotter.drawable = BlankDrawable()
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
        is EditPaperBackground.EditPaperEdge -> "Paper edge"
        else -> obj.toString()
    }

    val zoomProperty = property(1.0)

    fun zoomIn() = plotter.transformation.scaleIn(plotter.size / 2, TransformationInteraction.ANIMATION_TIME)

    fun zoomOut() = plotter.transformation.scaleOut(plotter.size / 2, TransformationInteraction.ANIMATION_TIME)

    fun resetZoom() = plotter.transformation.resetScale(plotter.size / 2, TransformationInteraction.ANIMATION_TIME)

}
