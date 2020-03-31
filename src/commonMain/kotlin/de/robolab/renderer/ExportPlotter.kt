package de.robolab.renderer

import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.BlankDrawable
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.theme.LightTheme
import de.robolab.renderer.utils.DrawContext
import de.robolab.renderer.utils.Pointer
import de.robolab.renderer.utils.Transformation
import de.westermann.kobserve.property.property

/**
 * @author lars
 */
class ExportPlotter(
        private val canvas: ICanvas,
        drawable: IDrawable = BlankDrawable
) : IPlotter {

    override val animationTime = 0.0
    override val transformation = Transformation()

    override val size: Dimension
        get() = Dimension(canvas.width, canvas.height)

    var drawable: IDrawable = BlankDrawable
        set(value) {
            field.onDetach(this)
            field = value
            field.onAttach(this)
        }

    private val context = DrawContext(canvas, transformation, LightTheme)

    override val pointerProperty = property(Pointer())
    override var pointer by pointerProperty

    override fun render(ms_offset: Double) {
        drawable.onUpdate(ms_offset)
        transformation.update(ms_offset)

        context.clear(context.theme.secondaryBackgroundColor)
        drawable.onDraw(context)
    }

    override fun updatePointer(mousePosition: Point) {
    }

    init {
        this.drawable = drawable
    }
}
