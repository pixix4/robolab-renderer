package de.robolab.client.ui.view

import de.robolab.client.app.controller.StatusBarController
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.controller.TabController
import de.robolab.client.ui.adapter.toFx
import de.robolab.client.ui.style.MainStyle
import de.robolab.client.ui.view.icon
import de.westermann.kobserve.property.mapBinding
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import tornadofx.*

class TabBar(private val tabController: TabController) : View() {

    private fun updateTabList(box: HBox) {
        box.clear()

        var lastTab: HBox? = null
        for (tab in tabController.tabList) {
            val hBox = HBox().apply {
                addClass(MainStyle.tabBarTab)
                bindClass(MainStyle.active, tab.isFocusedProperty)

                vgrow = Priority.ALWAYS
                hgrow = Priority.ALWAYS

                setOnMouseClicked {
                    tab.focus()
                }

                spacer()

                label(tab.nameProperty.toFx()) {
                    vgrow = Priority.ALWAYS
                }

                spacer()

                icon(MaterialIcon.CLOSE) {
                    vgrow = Priority.ALWAYS
                    setOnMouseClicked {
                        tab.close()
                        it.consume()
                    }
                }
            }
            box.add(hBox)
            lastTab = hBox
        }
        lastTab?.addPseudoClass("last")
    }

    override val root = hbox {
        hgrow = Priority.ALWAYS
        addClass(MainStyle.tabBar)

        val visibleFx = tabController.visibleProperty.toFx()

        visibleWhen(visibleFx)
        managedWhen(visibleFx)

        hbox {
            vgrow = Priority.ALWAYS
            addClass(MainStyle.tabBarTab)

            icon(tabController.fullscreenProperty.mapBinding {
                if (it) MaterialIcon.FULLSCREEN_EXIT else MaterialIcon.FULLSCREEN
            }.toFx()) {
                vgrow = Priority.ALWAYS
            }

            setOnMouseClicked {
                tabController.toggleFullscreen()
            }

            val fullscreenFx = tabController.fullscreenProperty.toFx()
            visibleWhen(fullscreenFx)
            managedWhen(fullscreenFx)
        }

        hbox {
            vgrow = Priority.ALWAYS
            addClass(MainStyle.tabBarTab)
            addClass(MainStyle.tabBarTabStatus)

            bindClass(
                MainStyle.successTab,
                tabController.statusColor.mapBinding { it == StatusBarController.StatusColor.SUCCESS })
            bindClass(
                MainStyle.warnTab,
                tabController.statusColor.mapBinding { it == StatusBarController.StatusColor.WARN })
            bindClass(
                MainStyle.errorTab,
                tabController.statusColor.mapBinding { it == StatusBarController.StatusColor.ERROR })

            icon(tabController.statusColor.mapBinding {
                when (it) {
                    StatusBarController.StatusColor.SUCCESS -> MaterialIcon.LINK
                    StatusBarController.StatusColor.WARN -> MaterialIcon.LINK_OFF
                    StatusBarController.StatusColor.ERROR -> MaterialIcon.LINK_OFF
                }
            }.toFx()) {
                vgrow = Priority.ALWAYS
            }

            setOnMouseClicked {
                tabController.onStatusAction()
            }

            val fullscreenFx = tabController.fullscreenProperty.toFx()
            visibleWhen(fullscreenFx)
            managedWhen(fullscreenFx)
        }


        hbox {
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS
            addClass(MainStyle.tabBarContainer)

            updateTabList(this)
            tabController.tabList.onChange {
                updateTabList(this)
            }
        }

        /*hbox {
            vgrow = Priority.ALWAYS
            addClass(MainStyle.tabBarTab)
            addClass(MainStyle.tabBarTabIcon)
            addPseudoClass("last")

            icon(MaterialIcon.ADD) {
                vgrow = Priority.ALWAYS
            }

            setOnMouseClicked {
                tabController.openNewTab()
            }
        }*/
    }
}
