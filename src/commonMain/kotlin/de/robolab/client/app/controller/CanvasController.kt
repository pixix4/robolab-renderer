package de.robolab.client.app.controller

import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.canvas.VirtualCanvas
import de.robolab.client.renderer.drawable.utils.normalizeRadiant
import de.robolab.client.renderer.drawable.utils.radiantToDegree
import de.robolab.client.renderer.utils.CommonTimer
import de.robolab.client.renderer.utils.Pointer
import de.robolab.client.renderer.utils.TransformationInteraction
import de.robolab.common.parser.toFixed
import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Path
import de.robolab.common.utils.Point
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.now
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
import de.westermann.kobserve.property.property
import kotlin.math.roundToInt

class CanvasController(
    private val activeTabProperty: ObservableValue<TabController.Tab?>
) {

    private val plotterWindowProperty = activeTabProperty.nullableFlatMapBinding {
        it?.plotterManager?.activePlotterProperty
    }

    private val virtualCanvas = VirtualCanvas()
    fun setupCanvas(canvas: ICanvas) {
        virtualCanvas.canvas = canvas
    }

    private val pointerProperty = property<Pointer?>(null)
    val pointerStringProperty = pointerProperty.mapBinding { pointer ->
        val list = mutableListOf<String>()

        if (pointer != null) {
            list += "Pointer: ${format(pointer.roundedPosition)}"
        }

        val rotation = plotterWindowProperty.value?.transformation?.rotation
        if (rotation != null) {
            val degree = (360.0 - rotation.normalizeRadiant().radiantToDegree()).roundToInt()
            if (degree in 1..359) {
                list += "Rotation: $degree°"
            }
        }

        if (plotterWindowProperty.value?.transformation?.flipView == true) {
            list += "Flipped"
        }

        list.filter { it.isNotBlank() }
    }

    private fun format(obj: Any): String = when (obj) {
        is Path -> "Path(${obj.source.x},${obj.source.y},${obj.sourceDirection.name.first()} -> ${obj.target.x},${obj.target.y},${obj.targetDirection.name.first()})"
        is Coordinate -> "Coordinate(${obj.x},${obj.y})"
        is Point -> "${obj.left.toFixed(2)},${obj.top.toFixed(2)}"
        else -> obj.toString()
    }

    val zoomProperty = property<Double>()

    fun zoomIn() {
        val plotter = plotterWindowProperty.value ?: return
        plotter.transformation.scaleIn(
            plotter.dimension / 2,
            TransformationInteraction.ANIMATION_TIME
        )
    }

    fun zoomOut() {
        val plotter = plotterWindowProperty.value ?: return
        plotter.transformation.scaleOut(
            plotter.dimension / 2,
            TransformationInteraction.ANIMATION_TIME
        )
    }

    fun resetZoom() {
        val plotter = plotterWindowProperty.value ?: return
        plotter.transformation.resetScale(
            plotter.dimension / 2,
            TransformationInteraction.ANIMATION_TIME
        )
    }

    private fun render(msOffset: Double) {
        activeTabProperty.value?.plotterManager?.render(msOffset)
    }

    private val timer = CommonTimer(60.0)

    init {
        timer.onRender(this::render)
        timer.start()

        var oldTab = activeTabProperty.value
        activeTabProperty.onChange.now {
            oldTab?.canvas?.canvas = null
            oldTab?.onDetach()
            oldTab = activeTabProperty.value
            oldTab?.onAttach()
            oldTab?.canvas?.canvas = virtualCanvas
        }

        pointerProperty.bind(plotterWindowProperty.nullableFlatMapBinding { it?.pointerProperty })
        zoomProperty.bind(plotterWindowProperty.nullableFlatMapBinding { it?.transformation?.scaleProperty })
    }
}
