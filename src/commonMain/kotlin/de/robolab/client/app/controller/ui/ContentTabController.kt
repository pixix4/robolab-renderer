package de.robolab.client.app.controller.ui

import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.renderer.canvas.VirtualCanvas
import de.robolab.client.renderer.plotter.SimplePlotterManager
import de.robolab.client.renderer.utils.IRenderInstance
import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class ContentTabController(
    private val parent: ContentSplitController.Node
) : IRenderInstance {

    val tabList = observableListOf<Tab>()
    val activeProperty: ObservableProperty<Tab>

    val canvas = VirtualCanvas()

    fun openNewTab(): Tab {
        parent.select()

        val tab = Tab()
        tabList += tab
        selectTab(tab)
        return tab
    }

    fun selectTab(tab: Tab) {
        parent.select()

        if (tab !in tabList) return
        val oldTab = activeProperty.value
        if (oldTab == tab) return

        oldTab.canvas.canvas = null
        oldTab.plotterManager.onDetach()
        activeProperty.value = tab
        tab.plotterManager.onAttach()
        tab.canvas.canvas = canvas
    }

    fun closeTab(tab: Tab) {
        parent.select()

        if (tab !in tabList) return
        if (activeProperty.value == tab) {
            selectNext()
        }
        tabList -= tab
        if (tabList.isEmpty()) {
            parent.close()
        }
    }

    fun selectNext() {
        parent.select()

        val tab = activeProperty.value
        val index = tabList.indexOfOrNull(tab) ?: return
        selectTab(tabList.getRotating(index + 1) ?: return)
    }

    fun selectPrev() {
        parent.select()

        val tab = activeProperty.value
        val index = tabList.indexOfOrNull(tab) ?: return
        selectTab(tabList.getRotating(index - 1) ?: return)
    }

    init {
        val tab = Tab()
        tabList += tab

        activeProperty = property(tab)
        tab.plotterManager.onAttach()
        tab.canvas.canvas = canvas
    }

    fun importTab(oldTab: Tab) {
        parent.select()

        val newTab = openNewTab()
        newTab.document = oldTab.document
    }

    override fun onRender(msOffset: Double): Boolean {
        return activeProperty.value.onRender(msOffset)
    }

    fun getTab(document: IPlanetDocument): Tab? {
        return tabList.find { it.document == document }
    }

    fun openDocument(document: IPlanetDocument, newTab: Boolean) {
        val tab = if (newTab) openNewTab() else activeProperty.value
        tab.document = document
    }

    inner class Tab : IRenderInstance {

        val canvas = VirtualCanvas()
        val plotterManager = SimplePlotterManager(canvas, PreferenceStorage.animationTime)

        val documentProperty = plotterManager.activePlotter.planetDocumentProperty
        var document by documentProperty

        val nameProperty = documentProperty.flatMapBinding { it.nameProperty }

        val activeProperty by lazy {
            this@ContentTabController.activeProperty.mapBinding { it == this }
        }

        fun select() {
            selectTab(this)
        }

        fun close() {
            closeTab(this)
        }

        override fun onRender(msOffset: Double): Boolean {
            return plotterManager.onRender(msOffset)
        }
    }
}


fun <T> List<T>.getRotating(index: Int): T? {
    if (size == 0) return null
    var i = index
    while (i < 0) {
        i += size
    }
    while (i >= size) {
        i -= size
    }
    return getOrNull(index)
}

fun <T> List<T>.indexOfOrNull(element: T): Int? {
    val index = indexOf(element)
    return if (index < 0) null else index
}
