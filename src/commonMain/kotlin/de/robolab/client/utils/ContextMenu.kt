package de.robolab.client.utils

import de.robolab.common.utils.Point


data class ContextMenu(
    val position: Point,
    val entry: ContextMenuList
)

interface ContextMenuEntry {
    val label: String
}

data class ContextMenuAction(
    override val label: String,
    val checked: Boolean?,
    val action: () -> Unit
) : ContextMenuEntry

data class ContextMenuList(
    override val label: String,
    val entries: List<ContextMenuEntry>,
    var parent: ContextMenuList? = null
) : ContextMenuEntry {
    init {
        entries.onEach {
            if (it is ContextMenuList) it.parent = this
        }
    }
}


@DslMarker
annotation class MenuMarker

@MenuMarker
class MenuBuilder(
    var name: String
) {

    val entries = mutableListOf<ContextMenuEntry>()

    fun menu(name: String, init: MenuBuilder.() -> Unit) {
        val menuBuilder = MenuBuilder(name)
        init(menuBuilder)
        entries += ContextMenuList(menuBuilder.name, menuBuilder.entries)
    }

    fun action(name: String, checked: Boolean? = null, action: () -> Unit) {
        entries += ContextMenuAction(name, checked, action)
    }
}

fun buildContextMenu(position: Point, name: String = "", init: MenuBuilder.() -> Unit): ContextMenu? {
    val menuBuilder = MenuBuilder(name)
    init(menuBuilder)

    if (menuBuilder.entries.isEmpty()) return null

    return ContextMenu(
        position,
        ContextMenuList(
            menuBuilder.name,
            menuBuilder.entries
        )
    )
}
