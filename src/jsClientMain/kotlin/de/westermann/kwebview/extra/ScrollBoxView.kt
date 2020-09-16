package de.westermann.kwebview.extra

import de.robolab.client.app.model.base.adjustBoxList
import de.robolab.client.app.model.base.resizeBoxListBox
import de.robolab.client.ui.views.ResizeView
import de.robolab.client.utils.runAsync
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.emit
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.boxView
import kotlinx.browser.window
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import kotlin.math.round

class ScrollBoxView : ViewCollection<View>() {

    private var userResized = false
    private val contentList: MutableList<Pane> = mutableListOf()

    val onResize = EventHandler<Unit>()

    inner class Pane(
        private val pane: BoxView,
        val initPercentageSize: Double,
        var size: Double,
        val dynamicBox: Boolean
    ) {

        val index: Int
            get() = contentList.indexOf(this)

        fun setHeight(height: Double) {
            size = height
            pane.style.height = "${height}px"
        }
    }

    fun resizeBox(initPercentageSize: Double, autoResize: Boolean = false, init: BoxView.() -> Unit): BoxView {
        var view: BoxView? = null
        val pane = boxView("scroll-box-entry") {
            val boxPane = Pane(this, initPercentageSize, 0.0, autoResize)

            if (contentList.isNotEmpty()) {
                +ResizeView("scroll-box-handler") { position, _ ->
                    updateBorder(boxPane.index - 1, position.y - this@ScrollBoxView.offsetTopTotal)
                }
            }

            view = boxView("scroll-box-content") {
                init()
            }

            contentList += boxPane
        }

        update()

        return view ?: pane
    }

    private val rootHeight: Double
        get() = clientHeight.toDouble()

    private fun updateBorder(borderIndex: Int, targetPosition: Double) {
        userResized = true

        val boxSizeList = contentList.map { it.size }.toMutableList()
        val targetListSize = rootHeight
        val minBoxSize = 40.0
        val dynamicBox = contentList.indexOfFirst { it.dynamicBox }.let { if (it < 0) null else it }

        resizeBoxListBox(boxSizeList, targetListSize, minBoxSize, dynamicBox, borderIndex, targetPosition)

        for ((size, box) in boxSizeList.zip(contentList)) {
            box.size = size
        }

        update()
    }

    private fun update() {
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

        onResize.emit()
    }

    init {
        window.addEventListener("resize", object : EventListener {
            override fun handleEvent(event: Event) {
                update()
            }
        })

        runAsync {
            update()
        }
    }
}

fun ViewCollection<in ScrollBoxView>.scrollBoxView(init: ScrollBoxView.() -> Unit): ScrollBoxView {
    val view = ScrollBoxView()
    append(view)
    init(view)
    return view
}
