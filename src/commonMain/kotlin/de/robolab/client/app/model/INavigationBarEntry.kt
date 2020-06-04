package de.robolab.client.app.model

import de.robolab.client.utils.ContextMenu
import de.robolab.common.utils.Point
import de.westermann.kobserve.base.ObservableValue

interface INavigationBarEntry {

    val titleProperty: ObservableValue<String>
    val subtitleProperty: ObservableValue<String>
    val tabNameProperty: ObservableValue<String>
    val unsavedChangesProperty: ObservableValue<Boolean>

    val hasContextMenu: Boolean

    fun buildContextMenu(position: Point): ContextMenu {
        throw UnsupportedOperationException()
    }

    val parent: INavigationBarGroup?
}
