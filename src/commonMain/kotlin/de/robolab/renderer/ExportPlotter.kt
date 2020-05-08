package de.robolab.renderer

import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.BlankDrawable
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.utils.DrawContext
import de.robolab.renderer.utils.Pointer
import de.robolab.renderer.utils.Transformation
import de.robolab.theme.ITheme
import de.robolab.theme.LightTheme
import de.westermann.kobserve.property.property

/**
 * @author lars
 */
class ExportPlotter(
        private val canvas: ICanvas,
        drawable: IDrawable = BlankDrawable(),
        theme: ITheme = LightTheme
) : IPlotter {

    override val animationTime = 0.0
    override val transformation = Transformation()

    override val size: Dimension
        get() = Dimension(canvas.width, canvas.height)

    var drawable: IDrawable = BlankDrawable()
        set(value) {
            field.onDetach(this)
            field = value
            field.onAttach(this)
        }

    override val context = DrawContext(canvas, transformation, theme)

    var theme: ITheme
        get() = context.theme
        set(value) {
            context.theme = value
        }

    override val pointerProperty = property<Pointer?>(null)
    override val pointer by pointerProperty

    override fun render(ms_offset: Double) {
        drawable.onUpdate(ms_offset)
        transformation.update(ms_offset)

        context.clear(context.theme.plotter.secondaryBackgroundColor)
        drawable.onDraw(context)
    }

    override fun updatePointer(mousePosition: Point?): Point? {
        return null
    }

    init {
        this.drawable = drawable
    }
}
