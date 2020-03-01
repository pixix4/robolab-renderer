package de.robolab.renderer

import de.robolab.renderer.drawable.BlankDrawable
import de.robolab.renderer.interaction.DefaultInteraction
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.drawable.IDrawable
import de.robolab.renderer.platform.ITimer
import de.robolab.renderer.theme.LightTheme

/**
 * @author lars
 */
class DefaultPlotter(
        canvas: ICanvas,
        timer: ITimer,
        var drawable: IDrawable = BlankDrawable
) {
    private val transformation = Transformation()

    private val context = DrawContext(canvas, transformation, LightTheme)


    private fun render(ms_offset: Double) {
        context.clear(context.theme.secondaryBackgroundColor)

        drawable.onUpdate(ms_offset)
        drawable.onDraw(context)
    }

    init {
        canvas.setListener(DefaultInteraction(transformation))

        timer.onRender(this::render)

        timer.start()
    }
}
