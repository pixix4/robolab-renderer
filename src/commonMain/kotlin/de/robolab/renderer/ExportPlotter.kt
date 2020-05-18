package de.robolab.renderer

import de.robolab.renderer.data.Dimension
import de.robolab.renderer.document.base.Document
import de.robolab.renderer.platform.ICanvas
import de.robolab.theme.ITheme
import de.robolab.theme.LightTheme

/**
 * @author lars
 */
class ExportPlotter(
        private val canvas: ICanvas,
        rootDocument: Document? = null,
        theme: ITheme = LightTheme
) : IPlotter(canvas, theme, 0.0) {

    override val size: Dimension
        get() = Dimension(canvas.width, canvas.height)

    init {
        this.rootDocument = rootDocument
    }
}
