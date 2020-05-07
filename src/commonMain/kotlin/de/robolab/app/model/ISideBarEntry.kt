package de.robolab.app.model

import de.robolab.renderer.data.Point
import de.robolab.utils.ContextMenu
import de.westermann.kobserve.base.ObservableValue

interface ISideBarEntry {

    val titleProperty: ObservableValue<String>
    val subtitleProperty: ObservableValue<String>
    val tabNameProperty: ObservableValue<String>
    val unsavedChangesProperty: ObservableValue<Boolean>

    val hasContextMenu: Boolean

    fun buildContextMenu(position: Point): ContextMenu {
        throw UnsupportedOperationException()
    }

    val parent: ISideBarGroup?
}
