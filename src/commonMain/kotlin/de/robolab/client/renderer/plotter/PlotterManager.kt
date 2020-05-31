package de.robolab.client.renderer.plotter

import de.robolab.client.renderer.canvas.ClippingCanvas
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.canvas.ICanvasListener
import de.robolab.client.renderer.events.*
import de.robolab.client.renderer.utils.CommonTimer
import de.robolab.client.theme.ITheme
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.utils.*
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlin.math.absoluteValue

class PlotterManager(
    private val canvas: ICanvas,
    private val animationTime: Double
) {

    private val timer = CommonTimer(60.0)

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
            )
        }
    }

    private fun createWindow(layout: Rectangle = Rectangle(0.0, 0.0, 1.0, 1.0)): Window {
        val canvas = ClippingCanvas(canvas, Rectangle.ZERO)
        val plotter = PlotterWindow(canvas, null, theme, animationTime)

        val window = Window(layout, canvas, plotter)
        window.updateClipping(this.canvas.dimension)
        return window
    }

    private var theme: ITheme = PreferenceStorage.selectedTheme.theme
    val windowList = mutableListOf(createWindow())

    private var hoveredWindow: Window? = null
    val activeWindowProperty = property(windowList.first())
    var activeWindow by activeWindowProperty
        private set

    fun setActive(window: Window) {
        if (activeWindow != window) {
            activeWindow = window
            requestRedraw = true
        }
    }

    val activePlotterProperty = activeWindowProperty.mapBinding { it.plotter }
    val activePlotter by activePlotterProperty

    private fun render(msOffset: Double) {
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
                window.canvas.endClip()

                if (window.layout.bottom < 1.0) {
                    if (window.layout.right < 1.0) {
                        canvas.strokeLine(
                            listOf(
                                window.canvas.clip.topRight,
                                window.canvas.clip.bottomRight,
                                window.canvas.clip.bottomLeft
                            ),
                            theme.ui.borderColor,
                            1.0
                        )
                    } else {
                        canvas.strokeLine(
                            listOf(
                                window.canvas.clip.bottomRight,
                                window.canvas.clip.bottomLeft
                            ),
                            theme.ui.borderColor,
                            1.0
                        )
                    }
                } else if (window.layout.right < 1.0) {
                    canvas.strokeLine(
                        listOf(
                            window.canvas.clip.topRight,
                            window.canvas.clip.bottomRight
                        ),
                        theme.ui.borderColor,
                        1.0
                    )
                }
            }
        }
        requestRedraw = false

        if (windows.size > 1) {
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

        val left = (lane.minBy { it.layout.left } ?: return).layout.left
        val leftMax = (lane.maxBy { it.layout.left } ?: return).layout.left
        val top = (lane.minBy { it.layout.top } ?: return).layout.top
        val topMax = (lane.maxBy { it.layout.top } ?: return).layout.top

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

    init {
        PreferenceStorage.selectedThemeProperty.onChange {
            theme = PreferenceStorage.selectedTheme.theme

            for (window in windowList) {
                window.plotter.theme = theme
            }

            requestRedraw = true
        }

        canvas.setListener(object : ICanvasListener {
            override fun onPointerDown(event: PointerEvent) {
                checkPointer(event, true)
                activeWindow.canvas.transformListener.onPointerDown(event)
            }

            override fun onPointerUp(event: PointerEvent) {
                activeWindow.canvas.transformListener.onPointerUp(event)
            }

            override fun onPointerMove(event: PointerEvent) {
                activeWindow.canvas.transformListener.onPointerMove(event)
            }

            override fun onPointerDrag(event: PointerEvent) {
                activeWindow.canvas.transformListener.onPointerDrag(event)
            }

            override fun onPointerSecondaryAction(event: PointerEvent) {
                checkPointer(event, true)
                activeWindow.canvas.transformListener.onPointerSecondaryAction(event)
            }

            override fun onScroll(event: ScrollEvent) {
                checkPointer(event, true)
                activeWindow.canvas.transformListener.onScroll(event)
            }

            override fun onZoom(event: ZoomEvent) {
                checkPointer(event, true)
                activeWindow.canvas.transformListener.onZoom(event)
            }

            override fun onRotate(event: RotateEvent) {
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
            }

            override fun onKeyPress(event: KeyEvent) {
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

        timer.onRender(this::render)
        timer.start()
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
