package de.robolab.renderer.drawable.edit

import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.drawable.planet.EditPlanetDrawable
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.platform.KeyCode
import de.robolab.renderer.platform.KeyEvent
import de.robolab.renderer.platform.PointerEvent
import de.robolab.renderer.utils.DrawContext
import de.robolab.renderer.utils.ITransformation
import de.robolab.renderer.utils.Transformation
import de.robolab.renderer.utils.TransformationCanvas
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.property.constProperty
import de.westermann.kobserve.property.mapBinding
import kotlin.math.max

class EditMenuDrawable(
        private val editPlanetDrawable: EditPlanetDrawable
) : IDrawable {

    class DrawMenuEntry(val menu: Menu, val list: MenuList) {
        val rect: Rectangle
        val polygon: List<Point>
        val entryAreas: List<Pair<MenuEntry, Rectangle>>

        lateinit var transformation: ITransformation
        lateinit var canvas: ICanvas

        fun getCanvas(context: DrawContext): ICanvas {
            if (!this::canvas.isInitialized) {
                transformation = TransformationOverlay(context.transformation, menu.position)
                canvas = TransformationCanvas(context.canvas, transformation)
            }
            return canvas
        }

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

    class TransformationOverlay(private val transformation: ITransformation, private val reference: Point) : ITransformation {
        override val pixelPerUnitDimension = transformation.pixelPerUnitDimension

        private val referenceCanvas = planetToCanvas(reference, Point.ZERO, 1.0, 0.0)

        override val translationProperty: ReadOnlyProperty<Point> = transformation.translationProperty.mapBinding {
            val p1 = transformation.planetToCanvas(reference)
            p1 - referenceCanvas
        }
        override val translation by translationProperty

        override val scaleProperty = constProperty(1.0)
        override val scale by scaleProperty

        override val rotationProperty = constProperty(0.0)
        override val rotation by rotationProperty

        override val gridWidth = transformation.gridWidth
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
        val canvas = menuEntry.getCanvas(context)

        canvas.fillPolygon(menuEntry.polygon, context.theme.primaryBackgroundColor)

        val topLeft = menuEntry.polygon.last()
        val topRight = menuEntry.polygon[menuEntry.polygon.lastIndex - 1]
        if (menuEntry.entryAreas.isNotEmpty()) {
            canvas.strokeLine(listOf(
                    Point(topLeft.left, topLeft.top - LINE_HEIGHT),
                    Point(topRight.left, topRight.top - LINE_HEIGHT)
            ), context.theme.gridColor, 0.01)
        }

        val origin = menuEntry.polygon.last()
        canvas.fillText(
                menuEntry.list.label,
                origin + Point(0.1, -LINE_HEIGHT / 2),
                context.theme.lineColor,
                alignment = ICanvas.FontAlignment.LEFT
        )

        for ((entry, rect) in menuEntry.entryAreas) {
            if (hoveredEntry == entry) {
                canvas.fillPolygon(
                        rect.toEdgeList(),
                        context.theme.highlightColor.a(0.1)
                )
            }

            canvas.fillText(
                    entry.label,
                    Point(rect.left + 0.1, rect.top + LINE_HEIGHT / 2),
                    context.theme.lineColor,
                    alignment = ICanvas.FontAlignment.LEFT
            )

            if (entry is MenuList) {
                canvas.fillText(
                        "▶",
                        Point(rect.right - 0.1, rect.top + LINE_HEIGHT / 2),
                        context.theme.gridTextColor,
                        alignment = ICanvas.FontAlignment.RIGHT
                )
            }
        }

        canvas.strokePolygon(menuEntry.polygon, context.theme.gridColor, 0.01)
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        val menuEntry = entryStack.lastOrNull() ?: return emptyList()
        val pointer = context.transformation.planetToCanvas(position)
        val p = menuEntry.transformation.canvasToPlanet(pointer)

        if (p !in menuEntry.rect) {
            return emptyList()
        }

        for ((entry, rect) in menuEntry.entryAreas) {
            if (p in rect) {
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
                val entry = DrawMenuEntry(menuEntry.menu, selected)
                entry.canvas = menuEntry.canvas
                entry.transformation = menuEntry.transformation
                entryStack = entryStack + entry
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
