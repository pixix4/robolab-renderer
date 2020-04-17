package de.robolab.app.model

import de.robolab.renderer.data.Point
import de.robolab.utils.ContextMenu
import de.robolab.utils.ContextMenuList
import de.robolab.utils.MenuBuilder
import de.westermann.kobserve.ReadOnlyProperty

interface ISideBarEntry {

    val titleProperty: ReadOnlyProperty<String>
    val subtitleProperty: ReadOnlyProperty<String>
    val tabNameProperty: ReadOnlyProperty<String>
    val unsavedChangesProperty: ReadOnlyProperty<Boolean>

    val hasContextMenu: Boolean

    fun buildContextMenu(position: Point): ContextMenu {
        throw UnsupportedOperationException()
    }

    val parent: ISideBarGroup?
}
