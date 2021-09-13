package de.robolab.client.app.controller.ui

import de.robolab.client.app.model.base.EmptyPlanetDocument
import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.renderer.drawable.utils.degreeToRadiant
import de.robolab.client.renderer.utils.CommonTimer
import de.robolab.client.renderer.utils.Pointer
import de.robolab.client.renderer.utils.Transformation
import de.robolab.client.renderer.utils.TransformationInteraction
import de.robolab.client.repl.*
import de.robolab.client.repl.base.parse1
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

        val windowNode = ReplCommandNode("window", "") {
            node("zoom", "Set zoom level") {
                action("in", "Zoom in") { _ ->
                    zoomIn()
                }
                action("out", "Zoom out") { _ ->
                    zoomOut()
                }
                action("reset", "Reset zoom to 100%") { _ ->
                    resetZoom()
                }
                action("set", "Set zoom level", IntParameter.param("percent")) { _, params ->
                    val percent = params.parse1<IntParameter>()
                    setZoom(percent.value)
                }
            }
            node("rotate", "Set rotation level") {
                action("clockwise", "Rotate clockwise", IntParameter.param("degree", optional = true)) { _, params ->
                    val degree = params.parse1<IntParameter?>()
                    rotateClockwise(degree?.value)
                }
                action("counter-clockwise", "Rotate counter clockwise") { _, params ->
                    val degree = params.parse1<IntParameter?>()
                    rotateCounterClockwise(degree?.value)
                }
                action("reset", "Reset rotation to 0") { _ ->
                    resetRotation()
                }
                action("set", "Set rotation in", IntParameter.param("degree")) { _, params ->
                    val degree = params.parse1<IntParameter>()
                    setRotation(degree.value)
                }
            }
            node("translate", "Set translation") {
                action("up", "Translate up", DoubleParameter.param("by")) { _, params ->
                    val by = params.parse1<DoubleParameter?>()
                    translate(Vector(0.0, 1.0), by?.value)
                }
                action("left", "Translate left", DoubleParameter.param("by")) { _, params ->
                    val by = params.parse1<DoubleParameter?>()
                    translate(Vector(1.0, 0.0), by?.value)
                }
                action("down", "Translate down", DoubleParameter.param("by")) { _, params ->
                    val by = params.parse1<DoubleParameter?>()
                    translate(Vector(0.0, -1.0), by?.value)
                }
                action("right", "Translate right", DoubleParameter.param("by")) { _, params ->
                    val by = params.parse1<DoubleParameter?>()
                    translate(Vector(-1.0, 0.0), by?.value)
                }
                action("reset", "Reset translation to center") { _ ->
                    center()
                }
            }
        }

        isPlanetVisibleProperty.onChange.now {
            if (isPlanetVisibleProperty.value) {
                ReplRootCommand += windowNode
            } else {
                ReplRootCommand -= windowNode
            }
        }
    }
}
