package de.robolab.renderer

import de.robolab.renderer.data.Dimension
import de.robolab.renderer.document.base.Document
import de.robolab.renderer.document.base.IView
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.platform.ITimer
import de.robolab.utils.Logger
import de.robolab.utils.PreferenceStorage
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
 * JS console call: `robolab.de.robolab.renderer.logRenderer()`
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
