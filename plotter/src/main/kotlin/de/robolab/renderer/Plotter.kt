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
class Plotter(
        canvas: ICanvas,
        private val timer: ITimer,
        var drawable: IDrawable = BlankDrawable
) {
    private val transformation = Transformation()

    private val context = DrawContext(canvas, transformation, LightTheme)


    private fun render(ms_offset: Double) {
        context.clear(context.theme.secondaryBackgroundColor)

        drawable.onUpdate(ms_offset)
        drawable.onDraw(context)
    }

    fun start() {
        timer.start()
    }

    fun stop() {
        timer.stop()
    }

    init {
        canvas.setListener(DefaultInteraction(transformation))

        timer.onRender(this::render)

        start()
    }

    companion object {
        const val POINT_SIZE = 0.2
        const val LINE_WIDTH = POINT_SIZE / 7.5
        const val TARGET_RADIUS = 0.25
        const val CURVE_FIRST_POINT = 0.15
        const val CURVE_SECOND_POINT = 0.3
        
        const val ANIMATION_TIME = 1000
    }
}
