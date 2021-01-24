package de.robolab.client.app.model.base

import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.utils.ContextMenu
import de.robolab.client.utils.MenuBuilder
import de.robolab.client.utils.buildContextMenu
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Point
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.list.asObservable
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

interface INavigationBarEntry {

    val nameProperty: ObservableValue<String>
    val subtitleProperty: ObservableValue<String>

    val enabledProperty: ObservableValue<Boolean>
    val statusIconProperty: ObservableValue<List<MaterialIcon>>

    val tab: INavigationBarTab

    fun MenuBuilder.contextMenu() {}
    fun generateContextMenuAt(position: Point): ContextMenu? {
        return buildContextMenu(position) {
            contextMenu()
        }
    }

    fun open(asNewTab: Boolean) {
        tab.openEntry(this, asNewTab)
    }

    suspend fun getRenderDataTimestamp(): Long = -1
    suspend fun <T : ICanvas> renderPreview(canvasCreator: (dimension: Dimension) -> T?): T? = null
}

interface INavigationBarList {

    val nameProperty: ObservableValue<String>

    val childrenProperty: ObservableList<INavigationBarEntry>

    val parent: INavigationBarList?
}

interface INavigationBarSearchList : INavigationBarList {

    val searchProperty: ObservableProperty<String>
}

abstract class INavigationBarTab(
    val nameProperty: ObservableValue<String>,
    val iconProperty: ObservableValue<MaterialIcon>,
    val supportedModes: List<String>
) {

    constructor(name: String, icon: MaterialIcon, supportedModes: List<String>) : this(
        property(name),
        property(icon),
        supportedModes
    )

    abstract fun selectMode(mode: String)
    abstract val modeProperty: ObservableValue<String>

    val searchProperty = property("")
    open fun submitSearch() {}

    val activeProperty = property<INavigationBarList>(EmptyNavigationBarList)

    open fun createSearchList(parent: INavigationBarList): INavigationBarSearchList? = null

    val childrenProperty = searchProperty.join(activeProperty) { search, active ->
        if (search.isEmpty()) {
            active.childrenProperty
        } else if (active is INavigationBarSearchList) {
            active.searchProperty.value = search
            active.childrenProperty
        } else {
            val searchList = createSearchList(active)

            if (searchList == null) {
                active.childrenProperty.filter {
                    it.nameProperty.value.contains(search, true)
                }.toMutableList().asObservable()
            } else {
                searchList.searchProperty.value = search
                activeProperty.value = searchList
                searchList.childrenProperty
            }
        }
    }

    val canGoBackProperty = activeProperty.mapBinding {
        it.parent != null
    }

    fun goBack() {
        val parent = activeProperty.value.parent ?: return
        activeProperty.value = parent
    }

    abstract fun openEntry(entry: INavigationBarEntry, asNewTab: Boolean)

    val labelProperty = nameProperty.join(activeProperty) { name, active ->
        val nameList = mutableListOf<String>()

        var l: INavigationBarList? = active
        while (l != null) {
            nameList += l.nameProperty.value
            l = l.parent
        }

        val nameStr = nameList.filter { it.isNotEmpty() }.reversed().joinToString("/")

        if (nameStr.isBlank()) {
            return@join name
        }

        nameStr
    }
}

object EmptyNavigationBarList : INavigationBarList {
    override val nameProperty: ObservableValue<String> = constObservable("")
    override val childrenProperty: ObservableList<INavigationBarEntry> = observableListOf()
    override val parent: INavigationBarList? = null

}

object EmptyNavigationBarTab : INavigationBarTab("Empty", MaterialIcon.HOURGLASS_EMPTY, emptyList()) {
    override fun openEntry(entry: INavigationBarEntry, asNewTab: Boolean) {
        throw NotImplementedError()
    }

    override fun selectMode(mode: String) {}
    override val modeProperty = constObservable("")
}
