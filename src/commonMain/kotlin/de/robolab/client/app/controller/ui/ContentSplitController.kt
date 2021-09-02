package de.robolab.client.app.controller.ui

import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.renderer.utils.IRenderInstance
import de.robolab.client.renderer.utils.onRender
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class ContentSplitController : IRenderInstance {

    val rootProperty: ObservableProperty<Entry>
    val activeNodeProperty: ObservableProperty<Node>

    fun openDocument(document: IPlanetDocument, newTab: Boolean) {
        val nodeList = serialize()
        for (node in nodeList) {
            val tab = node.content.getTab(document) ?: continue
            tab.select()
            return
        }

        activeNodeProperty.value.content.openDocument(document, newTab)
    }

    fun openDocumentAtIndex(document: IPlanetDocument, index: Int, newTab: Boolean) {
        val nodeList = serialize()
        val tab = nodeList.getOrNull(index) ?: return
        tab.select()
        tab.content.openDocument(document, newTab)
    }

    fun splitEntryVertical(node: Node = activeNodeProperty.value) = node.splitVertical()
    fun splitEntryHorizontal(node: Node = activeNodeProperty.value) = node.splitHorizontal()
    fun closeEntry(entry: Entry = activeNodeProperty.value, simplify: Boolean = true) {
        val parent = entry.parent

        if (entry is Container) {
            for (child in entry.entryList.toList()) {
                closeEntry(child, false)
            }
        }

        if (parent == null) {
            rootProperty.value = Node()
        } else {
            parent.removeEntry(entry)
        }

        if (simplify) {
            rootProperty.value.simplify()
        }

        selectNextEntry()
    }

    fun selectEntry(entry: Node) {
        activeNodeProperty.value = entry
    }

    fun setGridLayout(rowCount: Int, colCount: Int) {
        if (rowCount <= 0 || colCount <= 0) return

        val oldNodes = serialize().filterNot {
            it.isEmpty
        }
        val newNodes = (0 until rowCount * colCount).map { oldNodes.getOrNull(it) ?: Node() }

        val root = Container(Orientation.HORIZONTAL)

        for (col in 0 until colCount) {
            val colContainer = Container(Orientation.VERTICAL)
            root.addEntry(colContainer)

            for (row in 0 until rowCount) {
                val node = newNodes[row * colCount + col]
                colContainer.addEntry(node)
            }
        }

        //closeEntry(rootProperty.value)
        rootProperty.value = root

        for ((i, n) in oldNodes.drop(newNodes.size).withIndex()) {
            for (tab in n.content.tabList.toList()) {
                newNodes[i % (rowCount * colCount)].content.importTab(tab)
                //tab.close()
            }
        }
    }

    private fun serialize(): List<Node> {
        val stack = mutableListOf(
            when (val rootEntry = rootProperty.value) {
                is Node -> return listOf(rootEntry)
                is Container -> rootEntry
                else -> throw IllegalStateException()
            }
        )

        val result = mutableListOf<Node>()
        while (stack.isNotEmpty()) {
            val current = stack.removeAt(0)

            for (entry in current.entryList) {
                when (entry) {
                    is Node -> result += entry
                    is Container -> stack += entry
                    else -> throw IllegalStateException()
                }
            }
        }

        return result
    }

    private fun selectNextEntry() {
        val list = serialize()
        val index = list.indexOfOrNull(activeNodeProperty.value) ?: -1
        selectEntry(list.getRotating(index + 1) ?: return)
    }

    private fun selectPreviousEntry() {
        val list = serialize()
        val index = list.indexOfOrNull(activeNodeProperty.value) ?: 0
        selectEntry(list.getRotating(index - 1) ?: return)
    }

    fun selectRightEntry() = selectNextEntry()
    fun selectBottomEntry() = selectNextEntry()
    fun selectLeftEntry() = selectPreviousEntry()
    fun selectTopEntry() = selectPreviousEntry()

    override fun onRender(msOffset: Double): Boolean {
        return rootProperty.value.onRender(msOffset)
    }

    init {
        val node = Node()
        rootProperty = property(node)
        activeNodeProperty = property(node)
    }

    interface Entry : IRenderInstance {
        var parent: Container?

        fun simplify() {}
    }

    inner class Node : Entry {

        override var parent: Container? = null

        val content = ContentTabController(this)

        val activeProperty by lazy {
            activeNodeProperty.mapBinding { it == this }
        }

        fun close() {
            closeEntry(this)
        }

        fun select() {
            selectEntry(this)
        }

        private fun split(orientation: Orientation) {
            val p = parent

            val container = Container(orientation)

            if (p == null) {
                rootProperty.value = container
            } else {
                p.replace(this, container)
            }

            container.addEntry(this)
            container.addEntry(Node())

            rootProperty.value.simplify()
        }

        fun splitVertical() = split(Orientation.VERTICAL)

        fun splitHorizontal() = split(Orientation.HORIZONTAL)

        val isEmpty
            get() = content.isEmpty

        override fun onRender(msOffset: Double): Boolean {
            return content.onRender(msOffset)
        }
    }

    inner class Container(
        val orientation: Orientation,
    ) : Entry {

        override var parent: Container? = null
        val entryList = observableListOf<Entry>()

        fun addEntry(entry: Entry, index: Int = entryList.size) {
            if (entry in entryList) return
            entryList.add(index, entry)
            entry.parent = this
        }

        fun addEntry(entry: List<Entry>, index: Int = entryList.size) {
            for ((i, e) in entry.withIndex()) {
                addEntry(e, index + i)
            }
        }

        fun removeEntry(entry: Entry) {
            if (entry !in entryList) return
            entryList -= entry
            entry.parent = null
        }

        override fun simplify() {
            val p = parent

            if (p != null && (entryList.size <= 1 || orientation == p.orientation)) {
                p.replace(this, entryList)
            }

            for (e in entryList) {
                e.simplify()
            }
        }

        fun replace(old: Entry, new: Entry) {
            val index = entryList.indexOf(old)
            removeEntry(old)
            addEntry(new, index)
        }

        fun replace(old: Entry, new: List<Entry>) {
            val index = entryList.indexOf(old)
            removeEntry(old)
            addEntry(new, index)
        }

        override fun onRender(msOffset: Double): Boolean {
            return entryList.onRender(msOffset)
        }
    }

    enum class Orientation {
        VERTICAL, HORIZONTAL
    }
}
