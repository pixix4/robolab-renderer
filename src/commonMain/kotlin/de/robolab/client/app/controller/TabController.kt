package de.robolab.client.app.controller

import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.communication.mqtt.RobolabMqttConnection
import de.robolab.client.renderer.canvas.VirtualCanvas
import de.robolab.client.renderer.plotter.PlotterManager
import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.not
import de.westermann.kobserve.or
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
import de.westermann.kobserve.property.property

class TabController(
    private val connection: RobolabMqttConnection
) {

    val statusColor = connection.connectionStateProperty.mapBinding {
        when (it) {
            is RobolabMqttConnection.Connected -> StatusBarController.StatusColor.SUCCESS
            is RobolabMqttConnection.Connecting -> StatusBarController.StatusColor.WARN
            is RobolabMqttConnection.ConnectionLost -> StatusBarController.StatusColor.ERROR
            is RobolabMqttConnection.Disconnected -> StatusBarController.StatusColor.ERROR
            else -> StatusBarController.StatusColor.ERROR
        }
    }

    fun onStatusAction() {
        connection.connectionState.onAction()
    }

    val fullscreenProperty = property(false)

    fun toggleFullscreen() {
        fullscreenProperty.value = !fullscreenProperty.value
    }

    fun empty(): Boolean {
        return tabList.size <= 1 && tabList.all { it.empty() }
    }

    val tabList = observableListOf<Tab>()

    val visibleProperty =
        !PreferenceStorage.hideEmptyTabBarProperty or fullscreenProperty or tabList.mapBinding { it.size > 1 }

    val activeTabProperty = property<Tab>()
    val activeDocumentProperty = activeTabProperty.nullableFlatMapBinding {
        it?.plotterManager?.activePlotterProperty
    }.nullableFlatMapBinding {
        it?.planetDocumentProperty
    }

    fun openNewTab(): Tab {
        val tab = Tab()

        tabList += tab
        activeTabProperty.value = tab

        return tab
    }

    fun open(document: IPlanetDocument, asNewTab: Boolean) {
        for (tab in tabList) {
            if (tab.focus(document)) {
                if (tab != activeTabProperty.value) {
                    activeTabProperty.value = tab
                }
                return
            }
        }

        if (asNewTab) {
            val activeTab = activeTabProperty.value ?: openNewTab()
            if (tabList.size <= 1 && activeTab.plotterManager.windowList.size <= 1 && activeTab.plotterManager.activePlotter.document == null) {
                activeTab.open(document)
            } else {
                openNewTab().open(document)
            }
        } else {
            val tab = activeTabProperty.value ?: openNewTab()
            tab.open(document)
        }
    }

    init {
        PreferenceStorage.animationTimeProperty.onChange {
            for (tab in tabList) {
                tab.plotterManager.animationTime = PreferenceStorage.animationTime
            }
        }
    }

    inner class Tab {

        val isFocusedProperty = activeTabProperty.mapBinding { it == this }

        val canvas = VirtualCanvas()

        val plotterManager = PlotterManager(canvas, PreferenceStorage.animationTime)

        val nameProperty = plotterManager.activePlotterProperty.flatMapBinding {
            it.planetDocumentProperty
        }.nullableFlatMapBinding {
            it?.nameProperty
        }.mapBinding {
            it ?: ""
        }

        fun open(document: IPlanetDocument) {
            plotterManager.open(document)
        }

        fun focus() {
            if (activeTabProperty.value != this) {
                activeTabProperty.value = this
            }
        }

        fun close() {
            val index = tabList.indexOf(this)
            tabList -= this

            if (tabList.isEmpty()) {
                openNewTab()
            } else {
                if (activeTabProperty.value == this) {
                    activeTabProperty.value =
                        tabList.getOrNull(index) ?: tabList.getOrNull(index - 1) ?: tabList.first()
                }
            }
        }

        fun focus(document: IPlanetDocument): Boolean {
            for (window in plotterManager.windowList) {
                if (window.plotter.planetDocument == document) {
                    plotterManager.setActive(window)
                    focus()
                    return true
                }
            }
            return false
        }

        fun onDetach() {
            plotterManager.onDetach()
        }

        fun onAttach() {
            plotterManager.onAttach()
        }

        fun empty(): Boolean {
            return plotterManager.windowList.all { it.plotter.planetDocument != null }
        }
    }
}
