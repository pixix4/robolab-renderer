package de.robolab.client.app.controller

import de.robolab.client.app.model.ISideBarPlottable
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.plotter.DefaultPlotter
import de.robolab.client.renderer.utils.CommonTimer
import de.robolab.client.renderer.utils.Pointer
import de.robolab.client.renderer.utils.TransformationInteraction
import de.robolab.common.parser.toFixed
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Path
import de.robolab.common.utils.Point
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlin.math.PI
import kotlin.math.roundToInt

class CanvasController(
        private val selectedEntryProperty: ObservableProperty<ISideBarPlottable?>
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
            plotter.rootDocument = plottable?.document
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
        list.filter { it.isNotBlank() }
    }

    private fun format(obj: Any): String = when (obj) {
        is Path -> "Path(${obj.source.x},${obj.source.y},${obj.sourceDirection.name.first()} -> ${obj.target.x},${obj.target.y},${obj.targetDirection.name.first()})"
        is Coordinate -> "Coordinate(${obj.x},${obj.y})"
        is Point -> "${obj.left.toFixed(2)},${obj.top.toFixed(2)}"
        else -> obj.toString()
    }

    val zoomProperty = property(1.0)

    fun zoomIn() = plotter.transformation.scaleIn(plotter.size / 2, TransformationInteraction.ANIMATION_TIME)

    fun zoomOut() = plotter.transformation.scaleOut(plotter.size / 2, TransformationInteraction.ANIMATION_TIME)

    fun resetZoom() = plotter.transformation.resetScale(plotter.size / 2, TransformationInteraction.ANIMATION_TIME)

}
