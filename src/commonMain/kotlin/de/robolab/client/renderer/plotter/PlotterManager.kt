package de.robolab.client.renderer.plotter

import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.app.model.group.AbstractGroupAttemptPlanetDocument
import de.robolab.client.app.model.group.getDuration
import de.robolab.client.renderer.canvas.ClippingCanvas
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.canvas.ICanvasListener
import de.robolab.client.renderer.events.*
import de.robolab.client.theme.ITheme
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.utils.*
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class PlotterManager(
    private val canvas: ICanvas,
    animationTime: Double
) {

    var animationTime = animationTime
        set(value) {
            field = value
            for (window in windowList) {
                window.plotter.animationTime = value
            }
        }

    private var highlightActiveWindow = true

    class Window(
        layout: Rectangle,
        val canvas: ClippingCanvas,
        val plotter: PlotterWindow
    ) {
        var layout: Rectangle = layout
            private set
        private var dimension = Dimension.ONE

        fun resize(layout: Rectangle) {
            this.layout = layout
            updateClipping(this.dimension)
        }

        fun updateClipping(dimension: Dimension) {
            this.dimension = dimension
            canvas.clip = Rectangle(
                layout.left * dimension.width,
                layout.top * dimension.height,
                layout.width * dimension.width,
                layout.height * dimension.height
            ).rounded()
        }
    }

    private fun createWindow(layout: Rectangle = Rectangle(0.0, 0.0, 1.0, 1.0)): Window {
        val clippingCanvas = ClippingCanvas(canvas, Rectangle.ZERO)
        val plotter = PlotterWindow(clippingCanvas, null, theme, animationTime)

        val window = Window(layout, clippingCanvas, plotter)
        window.updateClipping(this.canvas.dimension)
        return window
    }

    private var theme: ITheme = PreferenceStorage.selectedTheme.theme
    val windowList = mutableListOf(createWindow())

    private var hoveredWindow: Window? = null
    private val activeWindowProperty = property(windowList.first())
    private var activeWindow by activeWindowProperty

    fun setActive(window: Window) {
        if (activeWindow != window) {
            activeWindow = window
            requestRedraw = true
        }
    }

    fun setActive(index: Int) {
        setActive(windowList.getOrNull(index) ?: return)
    }

    fun hideHighlight() {
        highlightActiveWindow = false
        requestRedraw = true
    }

    val activePlotterProperty = activeWindowProperty.mapBinding { it.plotter }
    val activePlotter by activePlotterProperty

    private var debugStatus = PreferenceStorage.debugStatus
    private var debugHierarchy = PreferenceStorage.debugHierarchy

    private val fpsWindow = DoubleArray(FPS_WINDOW_SIZE) { 0.0 }
    private var fps = 0.0
    private var fpsInt = 0
    private var index = 0

    fun render(msOffset: Double) {
        if (msOffset > 0.0) {
            val oldFps = fpsWindow[index]
            val newFps = 1000.0 / FPS_WINDOW_SIZE / msOffset
            fpsWindow[index++] = newFps
            index %= FPS_WINDOW_SIZE
            fps = fps - oldFps + newFps
            fpsInt = fps.roundToInt()
        }

        val windows = windowList
        if (requestRedraw) {
            canvas.fillRect(
                Rectangle.fromDimension(Point.ZERO, canvas.dimension).expand(1.0),
                theme.plotter.secondaryBackgroundColor
            )
        }

        for (window in windows) {
            val windowChanged = window.plotter.onUpdate(msOffset)

            if (windowChanged || requestRedraw) {
                window.canvas.startClip(window.canvas.clip)
                window.plotter.onDraw()
                if (debugHierarchy) {
                    window.plotter.onDebugDraw()
                }
                window.canvas.endClip()

                if (windowList.size > 1) {
                    val edges = mutableListOf<Pair<Point, Point>>()

                    if (window.layout.top > 0.0) {
                        edges += window.canvas.clip.topLeft to window.canvas.clip.topRight
                    }
                    if (window.layout.left > 0.0) {
                        edges += window.canvas.clip.topLeft to window.canvas.clip.bottomLeft
                    }
                    if (window.layout.bottom < 1.0) {
                        edges += window.canvas.clip.bottomLeft to window.canvas.clip.bottomRight
                    }
                    if (window.layout.right < 1.0) {
                        edges += window.canvas.clip.topRight to window.canvas.clip.bottomRight
                    }

                    for ((start, end) in edges) {
                        canvas.strokeLine(listOf(start, end), theme.ui.borderColor, 2.0)
                    }
                }
            }

            val planetDocument = window.plotter.planetDocument
            var name = window.plotter.planetDocument?.nameProperty?.value?.trim() ?: ""

            if (planetDocument != null && planetDocument is AbstractGroupAttemptPlanetDocument) {
                val extra = listOfNotNull(
                    planetDocument.planetNameProperty.value.let { if (it.isEmpty()) null else it },
                    planetDocument.duration.let { if (it.isEmpty()) null else it },
                )
                if (extra.isNotEmpty()) {
                    name += " " + extra.joinToString(", ", "(", ")")
                }
            }

            if ((windowChanged || requestRedraw) && windowList.size > 1 && name.isNotBlank() && name.isNotEmpty() || debugStatus) {
                val lines = if (debugStatus) listOfNotNull(
                    name,
                    "Active: ${windowChanged || requestRedraw}",
                    "FPS: $fpsInt"
                ) else listOfNotNull(
                    name
                )

                if (lines.isEmpty()) break

                val (backgroundColor, textColor) = if (window == activeWindow && highlightActiveWindow) {
                    theme.ui.themeColor.interpolate(theme.ui.primaryBackground, 0.3) to theme.ui.themePrimaryText
                } else {
                    theme.ui.tertiaryBackground to theme.ui.primaryTextColor
                }
                val radius = 10.0
                val width = 10.0 * (lines.map { it.length }.maxOrNull() ?: 0) + 24.0
                val height = 18.0 * lines.size + 10.0
                canvas.fillRect(
                    Rectangle(window.canvas.clip.left, window.canvas.clip.top, width - radius, height),
                    backgroundColor
                )
                canvas.fillRect(
                    Rectangle(
                        window.canvas.clip.left + width - radius - 10.0,
                        window.canvas.clip.top,
                        radius + 10.0,
                        height - radius
                    ),
                    backgroundColor
                )
                canvas.fillArc(
                    Point(window.canvas.clip.left + width - radius, window.canvas.clip.top + height - radius),
                    radius,
                    0.0,
                    2.0 * PI,
                    backgroundColor
                )
                for ((i, l) in lines.withIndex())
                    canvas.fillText(
                        l,
                        window.canvas.clip.topLeft + Point(12.0, 14.0 + 16.0 * i),
                        textColor,
                        fontSize = 16.0,
                        fontWeight = ICanvas.FontWeight.BOLD
                    )
            }
        }
        requestRedraw = false

        if (windows.size > 1 && highlightActiveWindow) {
            canvas.strokeRect(
                activeWindow.canvas.clip.shrink(1.0),
                theme.ui.themeColor,
                2.0
            )
        }
    }

    private var requestRedraw = false
    private fun checkPointer(event: PointerEvent, updateActiveWindow: Boolean) {
        val hovered = windowList.find { event.mousePoint in it.canvas.clip }

        if (hovered != hoveredWindow) {
            if (hoveredWindow == activeWindow) {
                activeWindow.canvas.transformListener.onPointerLeave(event)
            }

            hoveredWindow = hovered

            if (updateActiveWindow && hovered != null && hovered != activeWindow) {
                activeWindow = hovered
                activeWindow.canvas.transformListener.onPointerEnter(event)
                requestRedraw = true
            } else if (hoveredWindow == activeWindow) {
                activeWindow.canvas.transformListener.onPointerEnter(event)
            }
        } else if (updateActiveWindow && hovered != null && hovered != activeWindow) {
            activeWindow = hovered
            activeWindow.canvas.transformListener.onPointerEnter(event)
            requestRedraw = true
        }
    }

    fun split(vertical: Boolean) {
        val activeWindow = activeWindow

        val (first, second) = if (vertical) activeWindow.layout.splitVertical() else activeWindow.layout.splitHorizontal()
        val newWindow = createWindow(second)
        activeWindow.resize(first)
        windowList.add(windowList.indexOf(activeWindow) + 1, newWindow)
        setActive(newWindow)

        val lane = findLane(newWindow, activeWindow)

        if (lane.size > 2) {
            smoothLane(lane)
        }

        requestRedraw = true
    }

    private fun findSideViews(window: Window, compare: (Rectangle, Rectangle) -> Boolean): List<Window> {
        val next = windowList.find { compare(it.layout, window.layout) } ?: return listOf(window)
        return listOf(window) + findSideViews(next, compare)
    }

    private fun findLane(vararg window: Window): List<Window> {
        val laneRef = window.toList()
        val first = window.first()

        val horizontalLane =
            (findSideViews(first, Rectangle::leftOf) + findSideViews(first, Rectangle::rightOf)).distinct()
        if (horizontalLane.containsAll(laneRef)) {
            return horizontalLane.sortedBy {
                it.layout.left
            }
        }

        val verticalLane =
            (findSideViews(first, Rectangle::topOf) + findSideViews(first, Rectangle::bottomOf)).distinct()
        if (verticalLane.containsAll(laneRef)) {
            return verticalLane.sortedBy {
                it.layout.top
            }
        }

        return listOf(first)
    }

    private fun smoothLane(lane: List<Window>) {
        if (lane.size <= 1) return

        val left = (lane.minByOrNull { it.layout.left } ?: return).layout.left
        val leftMax = (lane.maxByOrNull { it.layout.left } ?: return).layout.left
        val top = (lane.minByOrNull { it.layout.top } ?: return).layout.top
        val topMax = (lane.maxByOrNull { it.layout.top } ?: return).layout.top

        if (left == leftMax) {
            val width = lane.first().layout.width
            val height = lane.sumByDouble { it.layout.height } / lane.size

            for ((index, window) in lane.withIndex()) {
                window.resize(
                    Rectangle(
                        left,
                        top + index * height,
                        width,
                        height
                    )
                )
            }
        } else if (top == topMax) {
            val width = lane.sumByDouble { it.layout.width } / lane.size
            val height = lane.first().layout.height

            for ((index, window) in lane.withIndex()) {
                window.resize(
                    Rectangle(
                        left + index * width,
                        top,
                        width,
                        height
                    )
                )
            }
        }
    }

    fun splitHorizontal() {
        split(false)
    }

    fun splitVertical() {
        split(true)
    }

    fun closeWindow() {
        if (windowList.size <= 1) return

        val activeWindow = activeWindow

        val select = windowList.find { it.layout topOf activeWindow.layout }
            ?: windowList.find { it.layout leftOf activeWindow.layout }
            ?: windowList.find { it.layout bottomOf activeWindow.layout }
            ?: windowList.find { it.layout rightOf activeWindow.layout } ?: return
        val lane = findLane(activeWindow, select) - activeWindow

        select.resize(select.layout union activeWindow.layout)
        setActive(select)
        windowList.remove(activeWindow)

        activeWindow.plotter.planetDocument?.onDetach()
        activeWindow.plotter.planetDocument?.onDestroy()

        if (lane.size > 1) {
            smoothLane(lane)
        }

        requestRedraw = true
    }

    fun setGridLayout(rowCount: Int, colCount: Int) {
        if (rowCount == 0 || colCount == 0) return

        val gridCellWidth = 1.0 / colCount.toDouble()
        val gridCellHeight = 1.0 / rowCount.toDouble()

        var index = 0
        for (row in 0 until rowCount) {
            val rowDouble = row.toDouble()
            for (col in 0 until colCount) {
                val colDouble = col.toDouble()

                val window = windowList.getOrNull(index)

                val rect = Rectangle(
                    colDouble * gridCellWidth,
                    rowDouble * gridCellHeight,
                    gridCellWidth,
                    gridCellHeight
                )

                if (window == null) {
                    val new = createWindow(rect)
                    windowList += new
                } else {
                    window.resize(rect)
                }

                index += 1
            }
        }

        while (index < windowList.size) {
            val window = windowList.removeAt(windowList.lastIndex)

            if (window == activeWindow) {
                setActive(windowList.first())
            }
        }

        requestRedraw = true
    }

    private var isAttached = false
    fun onDetach() {
        isAttached = false
        for (window in windowList) {
            window.plotter.planetDocument?.onDetach()
        }
    }

    fun onAttach() {
        isAttached = true
        for (window in windowList) {
            window.plotter.planetDocument?.onAttach()
        }
        requestRedraw = true
    }

    fun open(document: IPlanetDocument) {
        if (isAttached) {
            activePlotter.planetDocument?.onDetach()
        }

        activePlotter.planetDocument?.onDestroy()
        activePlotter.planetDocument = document
        document.onCreate()

        if (isAttached) {
            document.onAttach()
        }
    }

    init {
        PreferenceStorage.selectedThemeProperty.onChange {
            theme = PreferenceStorage.selectedTheme.theme

            for (window in windowList) {
                window.plotter.theme = theme
            }

            requestRedraw = true
        }

        PreferenceStorage.debugStatusProperty.onChange {
            debugStatus = PreferenceStorage.debugStatus

            if (debugStatus) {
                activeWindow.plotter.debug()
            }

            requestRedraw = true
        }
        PreferenceStorage.debugHierarchyProperty.onChange {
            debugHierarchy = PreferenceStorage.debugHierarchy

            if (debugHierarchy) {
                activeWindow.plotter.debug()
            }

            requestRedraw = true
        }
        PreferenceStorage.renderSenderGroupingProperty.onChange {
            requestRedraw = true
        }

        canvas.addListener(object : ICanvasListener {
            override fun onPointerDown(event: PointerEvent) {
                highlightActiveWindow = true
                checkPointer(event, true)
                activeWindow.canvas.transformListener.onPointerDown(event)
            }

            override fun onPointerUp(event: PointerEvent) {
                highlightActiveWindow = true
                activeWindow.canvas.transformListener.onPointerUp(event)
            }

            override fun onPointerMove(event: PointerEvent) {
                activeWindow.canvas.transformListener.onPointerMove(event)
            }

            override fun onPointerDrag(event: PointerEvent) {
                highlightActiveWindow = true
                activeWindow.canvas.transformListener.onPointerDrag(event)
            }

            override fun onPointerSecondaryAction(event: PointerEvent) {
                highlightActiveWindow = true
                checkPointer(event, true)
                activeWindow.canvas.transformListener.onPointerSecondaryAction(event)
            }

            override fun onScroll(event: ScrollEvent) {
                highlightActiveWindow = true
                checkPointer(event, true)
                activeWindow.canvas.transformListener.onScroll(event)
            }

            override fun onZoom(event: ZoomEvent) {
                highlightActiveWindow = true
                checkPointer(event, true)
                activeWindow.canvas.transformListener.onZoom(event)
            }

            override fun onRotate(event: RotateEvent) {
                highlightActiveWindow = true
                checkPointer(event, true)
                activeWindow.canvas.transformListener.onRotate(event)
            }

            override fun onPointerEnter(event: PointerEvent) {
                checkPointer(event, false)
            }

            override fun onPointerLeave(event: PointerEvent) {
                checkPointer(event, false)
            }

            override fun onResize(size: Dimension) {
                for (window in windowList) {
                    window.updateClipping(size)
                }
                requestRedraw = true
            }

            override fun onKeyPress(event: KeyEvent) {
                if (event.keyCode == KeyCode.ESCAPE && highlightActiveWindow) {
                    highlightActiveWindow = false
                    requestRedraw = true
                    return
                } else {
                    highlightActiveWindow = true
                }
                if (event.ctrlKey) {
                    when (event.keyCode) {
                        KeyCode.ARROW_UP -> {
                            val current = activeWindow.layout
                            val next = windowList.find {
                                it.layout.bottom.equalsDelta(current.top) &&
                                        it.layout.left < current.right &&
                                        it.layout.right > current.left
                            }

                            if (next != null) {
                                setActive(next)
                                return
                            }
                        }
                        KeyCode.ARROW_LEFT -> {
                            val current = activeWindow.layout
                            val next = windowList.find {
                                it.layout.right.equalsDelta(current.left) &&
                                        it.layout.top < current.bottom &&
                                        it.layout.bottom > current.top
                            }

                            if (next != null) {
                                setActive(next)
                                return
                            }
                        }
                        KeyCode.ARROW_DOWN -> {
                            val current = activeWindow.layout
                            val next = windowList.find {
                                it.layout.top.equalsDelta(current.bottom) &&
                                        it.layout.left < current.right &&
                                        it.layout.right > current.left
                            }

                            if (next != null) {
                                setActive(next)
                                return
                            }
                        }
                        KeyCode.ARROW_RIGHT -> {
                            val current = activeWindow.layout
                            val next = windowList.find {
                                it.layout.left.equalsDelta(current.right) &&
                                        it.layout.top < current.bottom &&
                                        it.layout.bottom > current.top
                            }

                            if (next != null) {
                                setActive(next)
                                return
                            }
                        }
                        KeyCode.V -> {
                            splitVertical()
                            return
                        }
                        KeyCode.H -> {
                            splitHorizontal()
                            return
                        }
                        KeyCode.B -> {
                            closeWindow()
                            return
                        }
                        else -> {
                        }
                    }
                }
                activeWindow.canvas.transformListener.onKeyPress(event)
            }

            override fun onKeyRelease(event: KeyEvent) {
                activeWindow.canvas.transformListener.onKeyRelease(event)
            }
        })
    }

    companion object {
        private const val FPS_WINDOW_SIZE: Int = 60
    }
}

private fun Double.equalsDelta(other: Double): Boolean {
    return (this - other).absoluteValue < 0.001
}

private infix fun Rectangle.topOf(other: Rectangle): Boolean {
    return bottom.equalsDelta(other.top) && left.equalsDelta(other.left) && right.equalsDelta(other.right)
}

private infix fun Rectangle.bottomOf(other: Rectangle): Boolean {
    return other topOf this
}

private infix fun Rectangle.leftOf(other: Rectangle): Boolean {
    return right.equalsDelta(other.left) && top.equalsDelta(other.top) && bottom.equalsDelta(other.bottom)
}

private infix fun Rectangle.rightOf(other: Rectangle): Boolean {
    return other leftOf this
}
