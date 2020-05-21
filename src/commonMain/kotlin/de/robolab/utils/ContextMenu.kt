package de.robolab.utils

import de.robolab.renderer.data.Point


data class ContextMenu(
        val position: Point,
        val entry: ContextMenuList
)

interface ContextMenuEntry {
    val label: String
}

data class ContextMenuAction(
        override val label: String,
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
class MenuBuilder {

    val entries = mutableListOf<ContextMenuEntry>()

    fun menu(name: String, init: MenuBuilder.() -> Unit) {
        val menuBuilder = MenuBuilder()
        init(menuBuilder)
        entries += ContextMenuList(name, menuBuilder.entries)
    }

    fun action(name: String, action: () -> Unit) {
        entries += ContextMenuAction(name, action)
    }
}

fun menuBilder(position: Point, name: String, init: MenuBuilder.() -> Unit): ContextMenu {
    val menuBuilder = MenuBuilder()
    init(menuBuilder)
    return ContextMenu(
            position,
            ContextMenuList(
                    name,
                    menuBuilder.entries
            )
    )
}
