package de.robolab.renderer

import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.BlankDrawable
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.platform.ITimer
import de.robolab.renderer.theme.LightTheme
import de.westermann.kobserve.property.property

/**
 * @author lars
 */
class DefaultPlotter(
        canvas: ICanvas,
        timer: ITimer,
        drawable: IDrawable = BlankDrawable,
        var animationTime: Double = 0.0
) {
    val transformation = Transformation()

    var drawable: IDrawable = BlankDrawable
        set(value) {
            field.onDetach(this)
            field = value
            field.onAttach(this)
        }

    private val interaction = TransformationInteraction(this)
    val size: Dimension
        get() = interaction.lastDimension

    private val context = DrawContext(canvas, transformation, LightTheme)

    val pointerProperty = property(Pointer())
    var pointer by pointerProperty

    private fun render(ms_offset: Double) {
        var changes = drawable.onUpdate(ms_offset)
        changes = transformation.update(ms_offset) || changes

        if (changes) {
            context.clear(context.theme.secondaryBackgroundColor)
            drawable.onDraw(context)
        }
    }

    fun getObjectsAtPosition(position: Point): List<Any> {
        return drawable.getObjectsAtPosition(context, position)
    }

    fun updatePointer(mousePosition: Point = pointer.mousePosition) {
        val position = transformation.canvasToPlanet(mousePosition)
        val elements = getObjectsAtPosition(position)

        pointer = Pointer(position, mousePosition, elements)
    }

    init {
        this.drawable = drawable
        canvas.setListener(interaction)
        transformation.onViewChange {
            updatePointer()
        }

        timer.onRender(this::render)
        timer.start()
    }
}
