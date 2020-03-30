package de.robolab.renderer.drawable.edit

import de.robolab.renderer.DrawContext
import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.platform.KeyCode
import de.robolab.renderer.platform.KeyEvent
import de.robolab.renderer.platform.PointerEvent
import kotlin.math.max

class EditMenuDrawable(
        private val editPlanetDrawable: EditPlanetDrawable
) : IDrawable {

    class DrawMenuEntry(val menu: Menu, val list: MenuList) {
        internal val rect: Rectangle
        internal val polygon: List<Point>
        internal val entryAreas: List<Pair<MenuEntry, Rectangle>>

        init {
            val width = max(list.label.length + 2, list.entries.asSequence().map { it.label.length }.max()
                    ?: 0) / 10.0
            val height = (1 + list.entries.size) * LINE_HEIGHT

            val origin = menu.position + Point(0.1, -height + LINE_HEIGHT / 2)

            polygon = listOf(
                    menu.position + Point(0.1, 0.06),
                    menu.position,
                    menu.position + Point(0.1, -0.06),
                    origin + Point(0.0, -0.1),
                    origin + Point(width, -0.1),
                    origin + Point(width, height),
                    origin + Point(0.0, height)
            )
            rect = Rectangle.fromDimension(
                    origin,
                    Dimension(width, height)
            )

            val firstRectOrigin = origin + Point(0.0, height - 2 * LINE_HEIGHT - 0.05)
            val rectOffset = Point(0.0, -LINE_HEIGHT)
            val rectDimension = Dimension(width, LINE_HEIGHT)

            entryAreas = list.entries.mapIndexed { index, entry ->
                val rect = Rectangle.fromDimension(
                        firstRectOrigin + (rectOffset * index),
                        rectDimension
                )
                entry to rect
            }
        }
    }

    private var hoveredEntry: MenuEntry? = null
    private var entryStack = emptyList<DrawMenuEntry>()
    private var lastTop: DrawMenuEntry? = null

    override fun onUpdate(ms_offset: Double): Boolean {
        val currentMenu = editPlanetDrawable.menu

        val visibleMenu = entryStack.lastOrNull()

        if (currentMenu == null) {
            if (visibleMenu == null) {
                return false
            }

            entryStack = emptyList()
            hoveredEntry = null
            lastTop = null
            return true
        }

        var changes = false
        if (currentMenu != visibleMenu?.menu) {
            changes = true
            entryStack = listOf(DrawMenuEntry(currentMenu, currentMenu.entry))
        }
        
        if (lastTop != entryStack.lastOrNull()) {
            lastTop = entryStack.lastOrNull()
            changes = true
        }

        val hovered = editPlanetDrawable.pointer.findObjectUnderPointer<MenuEntry>()
        if (hovered != hoveredEntry) {
            hoveredEntry = hovered
            changes = true
        }

        return changes
    }

    override fun onDraw(context: DrawContext) {
        val menuEntry = entryStack.lastOrNull() ?: return

        context.fillPolygon(menuEntry.polygon, context.theme.primaryBackgroundColor)

        val topLeft = menuEntry.polygon.last()
        val topRight = menuEntry.polygon[menuEntry.polygon.lastIndex - 1]
        if (menuEntry.entryAreas.isNotEmpty()) {
            context.strokeLine(listOf(
                    Point(topLeft.left, topLeft.top - LINE_HEIGHT),
                    Point(topRight.left, topRight.top - LINE_HEIGHT)
            ), context.theme.gridColor, 0.01)
        }

        val origin = menuEntry.polygon.last()
        context.fillText(
                menuEntry.list.label,
                origin + Point(0.1, -LINE_HEIGHT / 2),
                context.theme.lineColor,
                alignment = ICanvas.FontAlignment.LEFT
        )

        for ((entry, rect) in menuEntry.entryAreas) {

            if (hoveredEntry == entry) {
                context.fillRect(rect, context.theme.highlightColor.a(0.1))
            }

            context.fillText(
                    entry.label,
                    Point(rect.left + 0.1, rect.top + LINE_HEIGHT / 2),
                    context.theme.lineColor,
                    alignment = ICanvas.FontAlignment.LEFT
            )

            if (entry is MenuList) {
                context.fillText(
                        "▶",
                        Point(rect.right - 0.1, rect.top + LINE_HEIGHT / 2),
                        context.theme.gridTextColor,
                        alignment = ICanvas.FontAlignment.RIGHT
                )
            }
        }

        context.strokePolygon(menuEntry.polygon, context.theme.gridColor, 0.01)
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        val menuEntry = entryStack.lastOrNull() ?: return emptyList()

        if (position !in menuEntry.rect) {
            return emptyList()
        }

        for ((entry, rect) in menuEntry.entryAreas) {
            if (position in rect) {
                return listOf(entry, menuEntry.menu)
            }
        }

        return listOf(menuEntry.menu)
    }

    override fun onPointerDown(event: PointerEvent): Boolean {
        if (editPlanetDrawable.pointer.findObjectUnderPointer<Menu>() != null) {
            return true
        }
        return false
    }

    override fun onPointerUp(event: PointerEvent): Boolean {
        val menuEntry = entryStack.lastOrNull() ?: return false

        if (editPlanetDrawable.pointer.findObjectUnderPointer<Menu>() == null) {
            if (!event.hasMoved) {
                editPlanetDrawable.menu = null
                return true
            }

            return false
        }

        val selected = hoveredEntry
        if (selected != null) {
            if (selected is MenuAction) {
                editPlanetDrawable.menu = null
                selected.action()
            } else if (selected is MenuList) {
                entryStack = entryStack + DrawMenuEntry(menuEntry.menu, selected)
            }
        }
        return true
    }

    override fun onKeyPress(event: KeyEvent): Boolean {
        if (editPlanetDrawable.menu != null && event.keyCode == KeyCode.ESCAPE) {
            editPlanetDrawable.menu = null
            return true
        }

        return false
    }

    companion object {
        private const val LINE_HEIGHT = 0.3
    }
}

data class Menu(
        val position: Point,
        val entry: MenuList
)

interface MenuEntry {
    val label: String
}

data class MenuAction(
        override val label: String,
        val action: () -> Unit
) : MenuEntry

data class MenuList(
        override val label: String,
        val entries: List<MenuEntry>
) : MenuEntry


@DslMarker
annotation class MenuMarker

@MenuMarker
class MenuBuilder {

    val entries = mutableListOf<MenuEntry>()

    fun menu(name: String, init: MenuBuilder.() -> Unit) {
        val menuBuilder = MenuBuilder()
        init(menuBuilder)
        entries += MenuList(name, menuBuilder.entries)
    }

    fun action(name: String, action: () -> Unit) {
        entries += MenuAction(name, action)
    }
}

fun menu(position: Point, name: String, init: MenuBuilder.() -> Unit): Menu {
    val menuBuilder = MenuBuilder()
    init(menuBuilder)
    return Menu(
            position,
            MenuList(
                    name,
                    menuBuilder.entries
            )
    )
}
