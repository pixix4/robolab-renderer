package de.robolab.client.renderer.plotter

import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.view.base.Document
import de.robolab.client.theme.ITheme
import de.robolab.client.theme.LightTheme
import de.robolab.common.utils.Dimension

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
