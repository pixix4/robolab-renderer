package de.robolab.client.app.controller.ui

import de.robolab.client.app.model.base.EmptyPlanetDocument
import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.renderer.drawable.utils.degreeToRadiant
import de.robolab.client.renderer.utils.CommonTimer
import de.robolab.client.renderer.utils.Pointer
import de.robolab.client.renderer.utils.Transformation
import de.robolab.client.renderer.utils.TransformationInteraction
import de.robolab.client.repl.commands.window.WindowRotateCommand
import de.robolab.client.repl.commands.window.WindowTranslateCommand
import de.robolab.client.repl.commands.window.WindowZoomCommand
import de.robolab.common.utils.Vector
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.emit
import de.westermann.kobserve.event.now
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class ContentController {

    val content = ContentSplitController()

    fun openDocument(document: IPlanetDocument, newTab: Boolean) {
        content.openDocument(document, newTab)
    }

    fun openDocumentAtIndex(document: IPlanetDocument, index: Int, newTab: Boolean) {
        content.openDocumentAtIndex(document, index, newTab)
    }

    val activeTabProperty = content.activeNodeProperty.flatMapBinding { it.content.activeProperty }
    val plotterWindowProperty = activeTabProperty.flatMapBinding { it.plotterManager.activePlotterProperty }
    val isPlanetVisibleProperty =
        plotterWindowProperty.flatMapBinding { it.planetDocumentProperty }.mapBinding { it !is EmptyPlanetDocument }

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

    fun setZoom(percent: Int) {
        val plotter = plotterWindowProperty.value
        plotter.transformation.scaleTo(
            percent.toDouble() / 100.0,
            plotter.dimension / 2,
            TransformationInteraction.ANIMATION_TIME
        )
    }

    fun rotateClockwise(degree: Int? = null) {
        val plotter = plotterWindowProperty.value
        plotter.transformation.rotateBy(
            degree?.toDouble()?.degreeToRadiant() ?: TransformationInteraction.KEYBOARD_ROTATION,
            plotter.dimension / 2,
            TransformationInteraction.ANIMATION_TIME
        )
    }

    fun rotateCounterClockwise(degree: Int? = null) {
        val plotter = plotterWindowProperty.value
        plotter.transformation.rotateBy(
            -(degree?.toDouble()?.degreeToRadiant() ?: TransformationInteraction.KEYBOARD_ROTATION),
            plotter.dimension / 2,
            TransformationInteraction.ANIMATION_TIME
        )
    }

    fun resetRotation() {
        val plotter = plotterWindowProperty.value
        plotter.transformation.rotateTo(
            0.0,
            plotter.dimension / 2,
            TransformationInteraction.ANIMATION_TIME
        )
    }

    fun setRotation(degree: Int) {
        val plotter = plotterWindowProperty.value
        plotter.transformation.rotateTo(
            -degree.toDouble().degreeToRadiant(),
            plotter.dimension / 2,
            TransformationInteraction.ANIMATION_TIME
        )
    }

    fun translate(direction: Vector, length: Double? = null) {
        val l =
            if (length != null) length * Transformation.PIXEL_PER_UNIT else TransformationInteraction.KEYBOARD_TRANSLATION
        val plotter = plotterWindowProperty.value
        plotter.transformation.translateBy(
            direction.normalize() * l,
            TransformationInteraction.ANIMATION_TIME
        )
    }

    fun center() {
        val plotter = plotterWindowProperty.value
        plotter.planetDocument.centerPlanet()
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

        isPlanetVisibleProperty.onChange.now {
            if (isPlanetVisibleProperty.value) {
                WindowZoomCommand.bind(this)
                WindowRotateCommand.bind(this)
                WindowTranslateCommand.bind(this)
            } else {
                WindowZoomCommand.bind(null)
                WindowRotateCommand.bind(null)
                WindowTranslateCommand.bind(null)
            }
        }
    }
}
