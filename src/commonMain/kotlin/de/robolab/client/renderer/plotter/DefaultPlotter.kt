package de.robolab.client.renderer.plotter

import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.utils.ITimer
import de.robolab.client.renderer.utils.TransformationInteraction
import de.robolab.client.renderer.view.base.Document
import de.robolab.client.renderer.view.base.IView
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Logger
import kotlin.js.JsName

/**
 * @author lars
 */
class DefaultPlotter(
    canvas: ICanvas,
    private val timer: ITimer,
    rootDocument: Document? = null,
    animationTime: Double = 0.0
) : IPlotter(canvas, PreferenceStorage.selectedTheme.theme, animationTime) {

    private val interaction = TransformationInteraction(this)

    override val updateHover: Boolean
        get() = !interaction.isDrag

    override val size: Dimension
        get() = interaction.lastDimension

    override val fps: Double
        get() = timer.fps

    var forceRedraw = false
    override fun onUpdate(ms_offset: Double): Boolean {
        var changes = super.onUpdate(ms_offset)

        if (forceRedraw) {
            forceRedraw = false
            changes = true
        }

        return changes
    }

    init {
        this.rootDocument = rootDocument
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

        instance = this
    }

    companion object {
        var instance: DefaultPlotter? = null
    }
}

/**
 * JS console call: `robolab.de.robolab.client.renderer.plotter.logRenderer()`
 */
@Suppress("unused")
@JsName("logRenderer")
fun logRenderer() {
    val logger = Logger("DefaultPlotter")
    val document = DefaultPlotter.instance?.rootDocument ?: return

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
