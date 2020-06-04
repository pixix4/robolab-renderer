package de.robolab.client.app.controller

import de.robolab.client.app.model.INavigationBarPlottable
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.plotter.PlotterManager
import de.robolab.client.renderer.utils.Pointer
import de.robolab.client.renderer.utils.TransformationInteraction
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.parser.toFixed
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Path
import de.robolab.common.utils.Point
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlin.math.PI
import kotlin.math.roundToInt

class CanvasController(
    private val selectedEntryProperty: ObservableProperty<INavigationBarPlottable?>
) {

    lateinit var plotter: PlotterManager
    fun setupCanvas(canvas: ICanvas) {
        if (this::plotter.isInitialized) {
            throw IllegalStateException("Plotter is already initialized!")
        }

        plotter = PlotterManager(canvas, PreferenceStorage.animationTime)
        PreferenceStorage.animationTimeProperty.onChange {
            plotter.animationTime = PreferenceStorage.animationTime
        }

        plotter.activeWindowProperty.onChange {
            selectedEntryProperty.value = plotterMap[plotter.activeWindow]
        }
        pointerProperty.bind(plotter.activePlotterProperty.flatMapBinding { it.pointerProperty })
        zoomProperty.bind(plotter.activePlotterProperty.flatMapBinding { it.transformation.scaleProperty })
    }

    private var plotterMap = mapOf<PlotterManager.Window, INavigationBarPlottable>()
    fun open(plottable: INavigationBarPlottable) {
        val window = plotter.windowList.find { it.plotter.rootDocument == plottable.document }

        if (window != null) {
            plotter.setActive(window)
        } else {
            plotter.activeWindow.plotter.rootDocument = plottable.document
            plotterMap = plotterMap + (plotter.activeWindow to plottable)
            selectedEntryProperty.value = plottable
        }
    }

    val pointerProperty = property<Pointer?>(null)
    val pointerStringProperty = pointerProperty.mapBinding { pointer ->
        if (pointer == null) return@mapBinding emptyList<String>()

        val list = mutableListOf<String>()
        list.add("Pointer: ${format(pointer.roundedPosition)}")
        if (plotter.activePlotter.transformation.rotation != 0.0) {
            list.add("Rotation: ${((plotter.activePlotter.transformation.rotation / PI * 180) % 360).roundToInt()}%")
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

    fun zoomIn() = plotter.activePlotter.transformation.scaleIn(
        plotter.activePlotter.dimension / 2,
        TransformationInteraction.ANIMATION_TIME
    )

    fun zoomOut() = plotter.activePlotter.transformation.scaleOut(
        plotter.activePlotter.dimension / 2,
        TransformationInteraction.ANIMATION_TIME
    )

    fun resetZoom() = plotter.activePlotter.transformation.resetScale(
        plotter.activePlotter.dimension / 2,
        TransformationInteraction.ANIMATION_TIME
    )

}
