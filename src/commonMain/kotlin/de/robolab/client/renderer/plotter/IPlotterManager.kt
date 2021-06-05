package de.robolab.client.renderer.plotter

import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.utils.IRenderInstance
import de.robolab.client.theme.utils.ITheme
import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.base.ObservableValue

abstract class IPlotterManager(
    protected val canvas: ICanvas,
    animationTime: Double
) : IRenderInstance {

    abstract val plotterList: List<PlotterWindow>

    var animationTime = animationTime
        set(value) {
            field = value
            for (plotter in plotterList) {
                plotter.animationTime = value
            }
        }

    protected var theme: ITheme = PreferenceStorage.selectedTheme.theme

    abstract val activePlotterProperty: ObservableValue<PlotterWindow>
    abstract val activePlotter: PlotterWindow

    protected var debugStatus = PreferenceStorage.debugStatus
    protected var debugHierarchy = PreferenceStorage.debugHierarchy

    var requestRedraw = false

    private var isAttached = false
    fun onDetach() {
        isAttached = false
        for (plotter in plotterList) {
            plotter.planetDocument.onDetach()
        }
    }

    fun onAttach() {
        isAttached = true
        for (plotter in plotterList) {
            plotter.planetDocument.onAttach()
        }
        requestRedraw = true
    }

    fun open(document: IPlanetDocument) {
        if (isAttached) {
            activePlotter.planetDocument.onDetach()
        }

        activePlotter.planetDocument.onDestroy()
        activePlotter.planetDocument = document
        document.onCreate()

        if (isAttached) {
            document.onAttach()
        }
    }

    init {
        PreferenceStorage.selectedThemeProperty.onChange {
            theme = PreferenceStorage.selectedTheme.theme

            for (plotter in plotterList) {
                plotter.theme = theme
            }

            requestRedraw = true
        }

        PreferenceStorage.debugStatusProperty.onChange {
            debugStatus = PreferenceStorage.debugStatus

            if (debugStatus) {
                activePlotter.debug()
            }

            requestRedraw = true
        }
        PreferenceStorage.debugHierarchyProperty.onChange {
            debugHierarchy = PreferenceStorage.debugHierarchy

            if (debugHierarchy) {
                activePlotter.debug()
            }

            requestRedraw = true
        }
        PreferenceStorage.renderSenderGroupingProperty.onChange {
            requestRedraw = true
        }

    }
}
