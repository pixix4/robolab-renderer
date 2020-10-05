package de.robolab.client.renderer.plotter

import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.utils.Pointer
import de.robolab.client.renderer.utils.Transformation
import de.robolab.client.renderer.utils.TransformationInteraction
import de.robolab.client.renderer.view.base.Document
import de.robolab.client.renderer.view.base.IView
import de.robolab.client.theme.ITheme
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Logger
import de.robolab.common.utils.Point
import de.robolab.common.utils.Rectangle
import de.westermann.kobserve.property.nullableFlatMapBinding
import de.westermann.kobserve.property.property
import kotlin.math.PI
import kotlin.math.tan

class PlotterWindow(
    private val canvas: ICanvas,
    planetDocument: IPlanetDocument?,
    theme: ITheme,
    var animationTime: Double
) {

    val dimension: Dimension
        get() = canvas.dimension

    var theme: ITheme
        get() = context.theme
        set(value) {
            context.theme = value
            document?.requestRedraw()
        }

    val transformation = Transformation()
    val context = DrawContext(canvas, transformation, theme)

    private val interaction = TransformationInteraction(this)

    private var lastAttachedDocument: Document? = null
    val planetDocumentProperty = property<IPlanetDocument>()
    var planetDocument by planetDocumentProperty

    val documentProperty = planetDocumentProperty.nullableFlatMapBinding { it?.documentProperty }
    val document by documentProperty

    fun onUpdate(msOffset: Double): Boolean {
        var changes = false

        changes = document?.onUpdate(msOffset) == true || changes
        changes = transformation.update(msOffset) || changes

        return changes
    }

    private fun drawPlaceholder() {
        val stripWidth = 100.0
        val stripOffset = stripWidth * 2.3

        val dimension = canvas.dimension
        var start = 0.0
        val heightOffset = dimension.height / tan(PI * 0.3)
        while (start - heightOffset < dimension.width) {
            val points = listOf(
                Point(start, 0.0),
                Point(start + stripWidth, 0.0),
                Point(start + stripWidth - heightOffset, dimension.height),
                Point(start - heightOffset, dimension.height),
            )

            canvas.fillPolygon(
                points,
                theme.plotter.lineColor.a(0.03)
            )

            start += stripOffset
        }
    }

    fun onDraw() {
        context.canvas.fillRect(
            Rectangle(
                0.0, 0.0,
                dimension.width,
                dimension.height
            ),
            context.theme.plotter.secondaryBackgroundColor
        )
        val d = document
        if (d != null) {
            d.onDraw(context)
        } else {
            drawPlaceholder()
        }
        document?.onDraw(context)
    }

    fun onDebugDraw() {
        context.renderedViewCount = 0
        document?.onDebugDraw(context)

        //context.canvas.fillText("Active: $isActive", Point(16.0, 16.0), Color.RED)
        //context.canvas.fillText("FPS: ${fps.toInt()}", Point(16.0, 32.0), Color.RED)
        // context.canvas.fillText(
        //     "Rendered view count: ${context.renderedViewCount}",
        //     Point(16.0, 48.0),
        //     Color.RED
        // )
    }

    fun render(msOffset: Double) {
        val isActive = onUpdate(msOffset)
        if (isActive) {
            onDraw()
        }
    }

    val updateHover: Boolean
        get() = !interaction.isDrag

    val pointerProperty = property<Pointer?>(null)
    val pointer by pointerProperty
    fun updatePointer(mousePosition: Point? = pointer?.mousePosition): Point? {
        if (mousePosition == null) {
            pointerProperty.value = null
            return null
        }

        val canvasPosition = transformation.canvasToPlanet(mousePosition)
        if (updateHover) {
            val epsilon = 1.0 / transformation.scaledGridWidth * 10.0
            document?.updateHoveredView(canvasPosition, mousePosition, epsilon)
        }

        pointerProperty.value = Pointer(canvasPosition, mousePosition)
        return canvasPosition
    }

    init {
        documentProperty.onChange {
            lastAttachedDocument?.onDetach(this)
            lastAttachedDocument = document
            lastAttachedDocument?.onAttach(this)
        }
        this.planetDocument = planetDocument

        canvas.addListener(interaction)
        transformation.onViewChange {
            updatePointer()
        }
    }

    fun debug() {
        val logger = Logger("DefaultPlotter")
        val document = document ?: return

        val result = mutableListOf("")
        fun log(view: IView, depth: Int) {
            result += "    ".repeat(depth) + view

            for (v in view) {
                log(v, depth + 1)
            }
        }

        log(document, 0)

        logger.info(result.joinToString("\n"))
    }
}
