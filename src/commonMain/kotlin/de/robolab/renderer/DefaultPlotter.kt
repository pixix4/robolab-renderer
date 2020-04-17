package de.robolab.renderer

import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.BlankDrawable
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.platform.ITimer
import de.robolab.renderer.theme.ITheme
import de.robolab.renderer.utils.DrawContext
import de.robolab.renderer.utils.Pointer
import de.robolab.renderer.utils.Transformation
import de.robolab.utils.PreferenceStorage
import de.westermann.kobserve.property.property

/**
 * @author lars
 */
class DefaultPlotter(
        canvas: ICanvas,
        timer: ITimer,
        drawable: IDrawable = BlankDrawable(),
        override var animationTime: Double = 0.0
) : IPlotter {
    override val transformation = Transformation()

    var drawable: IDrawable = BlankDrawable()
        set(value) {
            field.onDetach(this)
            field = value
            field.onAttach(this)
        }

    private val interaction = TransformationInteraction(this)
    override val size: Dimension
        get() = interaction.lastDimension

    override val context = DrawContext(canvas, transformation, PreferenceStorage.selectedTheme.theme)

    var forceRedraw = false

    override val pointerProperty = property<Pointer?>(null)
    override var pointer by pointerProperty

    override fun render(ms_offset: Double) {
        var changes = drawable.onUpdate(ms_offset)
        changes = transformation.update(ms_offset) || changes
        if (forceRedraw) {
            forceRedraw = false
            changes = true
        }

        if (changes) {
            context.clear(context.theme.secondaryBackgroundColor)
            drawable.onDraw(context)
        }
    }

    fun getObjectsAtPosition(position: Point): List<Any> {
        return drawable.getObjectsAtPosition(context, position)
    }

    override fun updatePointer(mousePosition: Point?): Point? {
        if (mousePosition == null) {
            pointer = null
            return null
        }

        val position = transformation.canvasToPlanet(mousePosition)
        val elements = getObjectsAtPosition(position).distinct()

        pointer = Pointer(position, mousePosition, elements)
        return position
    }

    init {
        this.drawable = drawable
        canvas.setListener(interaction)
        transformation.onViewChange {
            updatePointer()
        }

        PreferenceStorage.selectedThemeProperty.onChange {
            context.theme = PreferenceStorage.selectedTheme.theme
            forceRedraw = true
        }

        timer.onRender(this::render)
        timer.start()
    }
}
