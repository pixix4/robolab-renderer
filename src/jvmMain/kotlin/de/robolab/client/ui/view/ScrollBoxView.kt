package de.robolab.client.ui.view

import de.robolab.client.app.model.base.adjustBoxList
import de.robolab.client.app.model.base.resizeBoxListBox
import javafx.event.EventTarget
import javafx.scene.Cursor
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import tornadofx.*
import kotlin.math.round

class ScrollBoxView : View() {

    private var userResized = false
    private val contentList: MutableList<Pane> = mutableListOf()

    override val root = vbox {
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS
    }

    inner class Pane(
        private val pane: AnchorPane,
        val initPercentageSize: Double,
        var size: Double,
        val dynamicBox: Boolean
    ) {

        val index: Int
            get() = contentList.indexOf(this)

        fun setHeight(height: Double) {
            size = height
            pane.prefHeight = height
        }
    }

    fun scrollBox(initPercentageSize: Double, autoResize: Boolean = false, init: VBox.() -> Unit) {
        resizeBox(initPercentageSize, autoResize) {
            scrollpane {
                hgrow = Priority.ALWAYS
                vgrow = Priority.ALWAYS

                vbox {
                    hgrow = Priority.ALWAYS
                    vgrow = Priority.ALWAYS

                    init()
                }
            }
        }
    }

    fun resizeBox(initPercentageSize: Double, autoResize: Boolean = false, init: VBox.() -> Unit) {
        val pane = anchorpane {
            hgrow = Priority.ALWAYS

            val boxPane = Pane(this, initPercentageSize, 0.0, autoResize)

            vbox {
                anchorpaneConstraints {
                    topAnchor = 0.0
                    leftAnchor = 0.0
                    rightAnchor = 0.0
                    bottomAnchor = 0.0
                }
                hgrow = Priority.ALWAYS
                vgrow = Priority.ALWAYS

                vbox {
                    hgrow = Priority.ALWAYS
                    vgrow = Priority.ALWAYS

                    init()
                }
            }

            if (contentList.isNotEmpty()) {
                hbox {
                    anchorpaneConstraints {
                        topAnchor = 0.0
                        leftAnchor = 0.0
                        rightAnchor = 0.0
                    }

                    minHeight = 10.0
                    prefHeight = 10.0
                    maxHeight = 10.0

                    style {
                        cursor = Cursor.N_RESIZE
                    }

                    setOnMouseDragged { event ->
                        if (event.button == javafx.scene.input.MouseButton.PRIMARY) {
                            updateBorder(boxPane.index - 1, event)
                        }
                    }
                }
            }

            contentList += boxPane
        }

        root += pane

        update()
    }

    private val rootHeight: Double
        get() = (root.parent as? Region)?.height ?: root.height

    private fun updateBorder(borderIndex: Int, event: MouseEvent) {
        userResized = true

        val boxSizeList = contentList.map { it.size }.toMutableList()
        val targetListSize = rootHeight
        val minBoxSize = 40.0
        val dynamicBox = contentList.indexOfFirst { it.dynamicBox }.let { if (it < 0) null else it }

        val bounds = root.localToScreen(0.0, 0.0)
        val targetPosition = event.screenY - bounds.y

        resizeBoxListBox(boxSizeList, targetListSize, minBoxSize, dynamicBox, borderIndex, targetPosition)

        for ((size, box) in boxSizeList.zip(contentList)) {
            box.size = size
        }

        update()
    }

    fun update() {
        val targetListSize = rootHeight

        if (!userResized) {
            for (box in contentList) {
                box.size = round(targetListSize * box.initPercentageSize)
            }
        }

        val boxSizeList = contentList.map { it.size }.toMutableList()
        val minBoxSize = 40.0
        val dynamicBox = contentList.indexOfFirst { it.dynamicBox }.let { if (it < 0) null else it }

        adjustBoxList(boxSizeList, targetListSize, minBoxSize, dynamicBox)

        for ((size, box) in boxSizeList.zip(contentList)) {
            box.setHeight(size)
        }
    }

    init {
        root.heightProperty().onChange {
            update()
        }
    }
}

fun EventTarget.scrollBoxView(op: ScrollBoxView.() -> Unit = {}): ScrollBoxView {
    val box = ScrollBoxView()
    addChildIfPossible(box.root)
    op(box)
    return box
}
