package de.robolab.client.ui.views

import de.robolab.client.app.controller.TabController
import de.robolab.client.app.model.base.MaterialIcon
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.iconView
import de.westermann.kwebview.components.textView

class TabBar(private val tabController: TabController) : ViewCollection<View>() {

    private val bar: BoxView

    private fun update() {
        bar.clear()

        val tabs = tabController.tabList

        for (tab in tabs) {
            bar.boxView("tab-bar-item") {
                classList.bind("active", tab.isFocusedProperty)

                textView(tab.nameProperty)
                onClick {
                    tab.focus()
                }

                iconView(MaterialIcon.CLOSE) {
                    classList += "tab-bar-item-close"

                    onClick {
                        tab.close()
                        it.preventDefault()
                        it.stopPropagation()
                    }
                }
            }
        }
    }

    init {
        boxView("tab-bar-item", "tab-bar-extra") {
            classList.bind("active", tabController.fullscreenProperty)
            iconView(MaterialIcon.FULLSCREEN_EXIT)
            onClick {
                tabController.toggleFullscreen()
            }
        }

        bar = boxView("tab-bar-content")
        update()
        tabController.tabList.onChange {
            update()
        }
    }
}
