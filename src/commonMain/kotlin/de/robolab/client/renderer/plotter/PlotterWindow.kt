package de.robolab.client.renderer.plotter

import de.robolab.client.app.model.base.EmptyPlanetDocument
import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.utils.*
import de.robolab.client.renderer.view.base.Document
import de.robolab.client.renderer.view.base.IView
import de.robolab.client.theme.utils.ITheme
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Logger
import de.robolab.common.utils.Vector
import de.robolab.common.utils.Rectangle
import de.westermann.kobserve.event.emit
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.property
import kotlin.math.PI
import kotlin.math.tan

class PlotterWindow(
    private val canvas: ICanvas,
    planetDocument: IPlanetDocument,
    theme: ITheme,
    var animationTime: Double
) : IRenderInstance {

    val dimension: Dimension
        get() = canvas.dimension

    var theme: ITheme
        get() = context.theme
        set(value) {
            context.theme = value
            document.requestRedraw()
        }

    val transformation: ITransformation = Transformation()
    val context = DrawContext(canvas, transformation, theme)

    private val interaction = TransformationInteraction(this)

    val planetDocumentProperty = property<IPlanetDocument>(EmptyPlanetDocument())
    var planetDocument by planetDocumentProperty

    val documentProperty = planetDocumentProperty.flatMapBinding { it.documentProperty }
    val document by documentProperty

    private var lastAttachedDocument: Document = document

    private var forceRedraw = false
    fun onUpdate(msOffset: Double): Boolean {
        var changes = false

        changes = document.onUpdate(msOffset) || changes
        changes = transformation.update(msOffset) || changes

        if (forceRedraw) {
            forceRedraw = false
            changes = true
        }

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
                Vector(start, 0.0),
                Vector(start + stripWidth, 0.0),
                Vector(start + stripWidth - heightOffset, dimension.height),
                Vector(start - heightOffset, dimension.height),
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
        if (d.drawPlaceholder) {
            drawPlaceholder()
        } else {
            d.onDraw(context)
        }
    }

    fun onDebugDraw() {
        context.renderedViewCount = 0
        document.onDebugDraw(context)

        //context.canvas.fillText("Active: $isActive", Point(16.0, 16.0), Color.RED)
        //context.canvas.fillText("FPS: ${fps.toInt()}", Point(16.0, 32.0), Color.RED)
        // context.canvas.fillText(
        //     "Rendered view count: ${context.renderedViewCount}",
        //     Point(16.0, 48.0),
        //     Color.RED
        // )
    }

    override fun onRender(msOffset: Double): Boolean {
        val isActive = onUpdate(msOffset)
        if (isActive) {
            onDraw()
        }
        return isActive
    }

    val updateHover: Boolean
        get() = !interaction.isDrag

    val pointerProperty = property<Pointer?>(null)
    val pointer by pointerProperty
    fun updatePointer(mousePosition: Vector? = pointer?.mousePosition): Vector? {
        if (mousePosition == null) {
            if (pointerProperty.value != null) {
                pointerProperty.value = null
            } else {
                pointerProperty.onChange.emit()
            }
            return null
        }

        val canvasPosition = transformation.canvasToPlanet(mousePosition)
        if (updateHover) {
            val epsilon = 1.0 / transformation.scaledGridWidth * 10.0
            document.updateHoveredView(canvasPosition, mousePosition, epsilon)
        }

        pointerProperty.value = Pointer(canvasPosition, mousePosition)
        return canvasPosition
    }

    init {
        documentProperty.onChange {
            lastAttachedDocument.onDetach(this)
            lastAttachedDocument = document
            lastAttachedDocument.onAttach(this)
            forceRedraw = true
        }
        this.planetDocument = planetDocument

        canvas.addListener(interaction)
        transformation.onViewChange {
            updatePointer()
        }
    }

    fun debug() {
        val logger = Logger("DefaultPlotter")
        val document = document

        val result = mutableListOf("")
        fun log(view: IView, depth: Int) {
            result += "    ".repeat(depth) + view

            for (v in view) {
                log(v, depth + 1)
            }
        }

        log(document, 0)

        logger.info {
            result.joinToString("\n")
        }
    }
}
