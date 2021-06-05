package de.robolab.client.app.model.base

import de.robolab.client.app.viewmodel.SideBarContentViewModel
import de.robolab.client.app.viewmodel.SideBarTabViewModel
import de.robolab.client.app.viewmodel.buildFormContent
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.utils.ContextMenu
import de.robolab.client.utils.MenuBuilder
import de.robolab.client.utils.buildContextMenu
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Vector
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.property

interface INavigationBarEntry {

    val nameProperty: ObservableValue<String>
    val subtitleProperty: ObservableValue<String>

    val enabledProperty: ObservableValue<Boolean>
    val statusIconProperty: ObservableValue<List<MaterialIcon>>

    val tab: INavigationBarTab

    fun MenuBuilder.contextMenu() {}
    fun generateContextMenuAt(position: Vector): ContextMenu? {
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

interface INavigationBarList : SideBarContentViewModel {

    val childrenProperty: ObservableList<INavigationBarEntry>
}

interface INavigationBarSearchList : INavigationBarList {

    val searchProperty: ObservableProperty<String>
}

abstract class INavigationBarTab(
    nameProperty: ObservableValue<String>,
    iconProperty: ObservableValue<MaterialIcon>,
) : SideBarTabViewModel(nameProperty, iconProperty) {

    constructor(
        name: String,
        icon: MaterialIcon,
    ) : this(
        property(name),
        property(icon),
    )

    abstract fun openEntry(entry: INavigationBarEntry, asNewTab: Boolean)

    val children: List<INavigationBarEntry>
        get() {
            val current = contentProperty.value
            return if (current is INavigationBarList) current.childrenProperty.toList() else emptyList()
        }
}

object EmptyNavigationBarList : INavigationBarList {
    override val nameProperty: ObservableValue<String> = constObservable("")
    override val childrenProperty: ObservableList<INavigationBarEntry> = observableListOf()
    override val parent: INavigationBarList? = null
}

object EmptyNavigationBarTab : INavigationBarTab("Empty", MaterialIcon.HOURGLASS_EMPTY) {
    override val contentProperty: ObservableProperty<SideBarContentViewModel> = property(EmptyNavigationBarList)
    override val topToolBar = buildFormContent { }
    override val bottomToolBar = buildFormContent { }
    override fun openEntry(entry: INavigationBarEntry, asNewTab: Boolean) {}
}
