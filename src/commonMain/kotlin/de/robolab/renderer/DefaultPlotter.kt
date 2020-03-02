package de.robolab.renderer

import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.BlankDrawable
import de.robolab.renderer.interaction.DefaultInteraction
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.drawable.IDrawable
import de.robolab.renderer.interaction.CompassInteraction
import de.robolab.renderer.interaction.CompositionInteraction
import de.robolab.renderer.platform.ITimer
import de.robolab.renderer.theme.LightTheme
import de.westermann.kobserve.property.property

/**
 * @author lars
 */
class DefaultPlotter(
        canvas: ICanvas,
        timer: ITimer,
        var drawable: IDrawable = BlankDrawable,
        var animationTime: Double = 0.0
) {
    private val transformation = Transformation()

    private val interaction = CompositionInteraction(
            CompassInteraction(transformation),
            DefaultInteraction(transformation, this)
    )

    private val context = DrawContext(canvas, transformation, LightTheme)

    val pointerProperty = property(Pointer())
    var pointer by pointerProperty

    private fun render(ms_offset: Double) {
        context.clear(context.theme.secondaryBackgroundColor)

        drawable.onUpdate(ms_offset)
        interaction.onUpdate(ms_offset)
        drawable.onDraw(context)
    }

    fun getObjectAtPosition(position: Point): Any? {
        return drawable.getObjectAtPosition(context, position)
    }

    init {
        canvas.setListener(interaction)

        timer.onRender(this::render)

        timer.start()
    }
}
