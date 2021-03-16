package de.robolab.client.ui.views

import de.robolab.client.app.controller.ui.ContentTabController
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.renderer.canvas.ICanvasListener
import de.robolab.client.renderer.events.PointerEvent
import de.robolab.client.ui.adapter.InteractiveWebCanvas
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.*
import de.westermann.kwebview.extra.listFactory

class ContentTabView(
    private val content: ContentTabController
) : ViewCollection<View>() {

    private val canvas = Canvas()
    private val webCanvas = InteractiveWebCanvas(canvas)

    private var lastSize = clientWidth to clientHeight

    fun checkSizeChange() {
        val newSize = clientWidth to clientHeight

        if (newSize != lastSize) {
            lastSize = newSize
            canvas.updateSize()
        }
    }

    init {
        content.canvas.canvas = webCanvas

        webCanvas.addListener(object: ICanvasListener {
            override fun onPointerDown(event: PointerEvent) {
                content.selectTab(content.activeProperty.value)
            }
        })

        boxView("tab-bar-view") {
            listFactory(content.tabList, { tab ->
                BoxView().apply {
                    classList += "tab-bar-view-item"
                    classList += "tab-bar-view-item-labeled"
                    classList.bind("active", tab.activeProperty)

                    textView(tab.nameProperty) {
                        classList += "tab-bar-view-label"
                    }

                    iconView(MaterialIcon.CLOSE) {
                        classList += "tab-bar-view-close"
                        onClick {
                            it.stopPropagation()
                            tab.close()
                        }
                    }

                    onClick {
                        tab.select()
                    }
                }
            })
        }
        boxView {
            +canvas
        }
    }
}
