package de.robolab.renderer

import de.robolab.app.model.file.toFixed
import de.robolab.renderer.data.Color
import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point
import de.robolab.renderer.document.base.Document
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.utils.DrawContext
import de.robolab.renderer.utils.Pointer
import de.robolab.renderer.utils.Transformation
import de.robolab.theme.ITheme
import de.robolab.utils.PreferenceStorage
import de.westermann.kobserve.property.property

abstract class IPlotter(
        canvas: ICanvas,
        theme: ITheme,
        var animationTime: Double
) {


    abstract val size: Dimension
    
    open val fps: Double = 0.0

    val transformation = Transformation()
    val context = DrawContext(canvas, transformation, theme, PreferenceStorage.debugMode)
    
    var rootDocument: Document? = null
        set(value) {
            field?.onDetach(this)
            field = value
            field?.onAttach(this)
        }

    open fun onUpdate(ms_offset: Double): Boolean {
        var changes = false

        changes = rootDocument?.onUpdate(ms_offset) == true || changes
        changes = transformation.update(ms_offset) || changes

        return changes
    }

    fun render(ms_offset: Double) {
        val isActive = onUpdate(ms_offset)
        if (isActive || context.debug) {
            context.clear(context.theme.plotter.secondaryBackgroundColor)
            rootDocument?.onDraw(context)
            
            if (context.debug) {
                context.renderedViewCount = 0
                rootDocument?.onDebugDraw(context)

                context.canvas.fillText("Active: $isActive", Point(16.0, 16.0), Color.RED)
                context.canvas.fillText("FPS: ${fps.toInt()}", Point(16.0, 32.0), Color.RED)
                context.canvas.fillText("Rendered view count: ${context.renderedViewCount}", Point(16.0, 48.0), Color.RED)
            }
        }
    }
    
    open val updateHover = true

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
            rootDocument?.updateHoveredView(canvasPosition, mousePosition, epsilon)
        }

        pointerProperty.value = Pointer(canvasPosition, mousePosition)
        return canvasPosition
    }
    
    init {
        PreferenceStorage.debugModeProperty.onChange {
            context.debug = PreferenceStorage.debugMode
            rootDocument?.requestRedraw()
        }
    }
}
