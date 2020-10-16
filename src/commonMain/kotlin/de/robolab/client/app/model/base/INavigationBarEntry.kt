package de.robolab.client.app.model.base

import de.robolab.client.utils.ContextMenu
import de.robolab.common.utils.Point
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue

interface INavigationBarEntry {

    val nameProperty: ObservableValue<String>
    val subtitleProperty: ObservableValue<String>

    val enabledProperty: ObservableValue<Boolean>
    val statusIconProperty: ObservableValue<List<MaterialIcon>>

    fun contextMenu(position: Point): ContextMenu? {
        return null
    }

    fun open(asNewTab: Boolean)
}

interface INavigationBarList {

    val parentNameProperty: ObservableValue<String?>

    fun openParent()

    val childrenProperty: ObservableList<INavigationBarEntry>
}

interface INavigationBarEntryRoot {

    val searchProperty: ObservableProperty<String>

    fun submitSearch()

    val parentNameProperty: ObservableValue<String?>

    fun openParent()

    val childrenProperty: ObservableValue<ObservableList<INavigationBarEntry>>
}
