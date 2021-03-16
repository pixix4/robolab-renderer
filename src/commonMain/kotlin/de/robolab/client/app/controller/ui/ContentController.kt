package de.robolab.client.app.controller.ui

import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.renderer.utils.CommonTimer
import de.robolab.client.renderer.utils.Pointer
import de.robolab.client.renderer.utils.TransformationInteraction
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.emit
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.property

class ContentController {

    val content = ContentSplitController()

    fun openDocument(document: IPlanetDocument, newTab: Boolean) {
        content.openDocument(document, newTab)
    }

    val activeTabProperty = content.activeNodeProperty.flatMapBinding { it.content.activeProperty }
    val plotterWindowProperty = activeTabProperty.flatMapBinding { it.plotterManager.activePlotterProperty }

    val pointerProperty = property<Pointer?>(null)

    val zoomProperty = property<Double>()

    fun zoomIn() {
        val plotter = plotterWindowProperty.value
        plotter.transformation.scaleIn(
            plotter.dimension / 2,
            TransformationInteraction.ANIMATION_TIME
        )
    }

    fun zoomOut() {
        val plotter = plotterWindowProperty.value
        plotter.transformation.scaleOut(
            plotter.dimension / 2,
            TransformationInteraction.ANIMATION_TIME
        )
    }

    fun resetZoom() {
        val plotter = plotterWindowProperty.value
        plotter.transformation.resetScale(
            plotter.dimension / 2,
            TransformationInteraction.ANIMATION_TIME
        )
    }

    val onRender = EventHandler<Unit>()
    private fun render(msOffset: Double) {
        onRender.emit()
        content.onRender(msOffset)
    }

    private val timer = CommonTimer(60.0)

    init {
        timer.onRender(this::render)
        timer.start()

        pointerProperty.bind(plotterWindowProperty.flatMapBinding { it.pointerProperty })
        zoomProperty.bind(plotterWindowProperty.flatMapBinding { it.transformation.scaleProperty })
    }
}
