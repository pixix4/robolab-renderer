package de.robolab.client.ui.view

import de.robolab.client.app.controller.InfoBarController
import de.robolab.client.app.controller.TraverserBarController
import de.robolab.client.app.model.file.InfoBarFileEdit
import de.robolab.client.app.model.file.InfoBarFilePaper
import de.robolab.client.app.model.file.InfoBarFileTraverse
import de.robolab.client.app.model.file.InfoBarFileView
import de.robolab.client.app.model.group.InfoBarGroupMessages
import de.robolab.client.app.model.room.InfoBarRoomRobots
import de.robolab.client.ui.style.MainStyle
import de.robolab.client.ui.utils.icon
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.mapBinding
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

class InfoBar(private val infoBarController: InfoBarController) : View() {

    private fun updateTabs(tabBox: HBox) {
        tabBox.clear()

        val list = infoBarController.infoBarTabsProperty.value ?: return

        var last: HBox? = null
        tabBox.spacer()
        for (tab in list) {
            last = tabBox.hbox {
                addClass(MainStyle.tabBarTab)
                bindClass(MainStyle.active, infoBarController.infoBarActiveTabProperty.mapBinding { it == tab })

                setOnMouseClicked {
                    tab.open()
                }

                spacer()
                icon(tab.icon)
                tooltip(tab.tooltip)
                spacer()

                hgrow = Priority.ALWAYS
            }
        }
        last?.addPseudoClass("last")
        tabBox.spacer()
    }

    private fun updateContent(contentBox: VBox) {
        contentBox.clear()

        val content = infoBarController.infoBarContentProperty.value ?: return

        when (content) {
            is InfoBarFileView -> {
                contentBox.add(InfoBarFileViewView(content))
            }
            is InfoBarFilePaper -> {
                contentBox.add(InfoBarFilePaperView(content))
            }
            is InfoBarFileEdit -> {
                contentBox.add(InfoBarFileEditView(content))
            }
            is InfoBarFileTraverse -> {
                if (content.traverserProperty.value == null) {
                    content.traverse()
                }
                contentBox.add(NullableViewContainer(content.traverserProperty))
            }
            is InfoBarGroupMessages -> {
                contentBox.add(InfoBarGroupMessagesView(content))
            }
            is InfoBarRoomRobots -> {
                contentBox.add(InfoBarRoomRobotsView(content))
            }
        }
    }

    override val root = vbox {
        addClass(MainStyle.infoBar)
        vgrow = Priority.ALWAYS
        minWidth = 200.0

        hbox {
            addClass(MainStyle.tabBar)
            addClass(MainStyle.tabBarSide)
            hgrow = Priority.ALWAYS

            infoBarController.infoBarTabsProperty.onChange {
                updateTabs(this)
            }
            updateTabs(this)
        }

        vbox {
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS

            infoBarController.infoBarContentProperty.onChange {
                updateContent(this)
            }
            updateContent(this)
        }
    }
}

class NullableViewContainer(private val traverserProperty: ObservableValue<TraverserBarController?>) : View() {

    private var prop: ObservableProperty<TraverserBarController>? = null
    private var view: InfoBarFileTraverseView? = null

    private fun updateView() {
        val traverser = traverserProperty.value

        if (traverser == null) {
            root.clear()
            return
        }

        if (prop == null) {
            prop = de.westermann.kobserve.property.property(traverser)
        } else {
            prop?.value = traverser
        }
        if (view == null) {
            view = InfoBarFileTraverseView(prop!!)
        }

        root.add(view!!)
    }

    override val root = vbox { }

    init {
        traverserProperty.onChange {
            updateView()
        }
        updateView()
    }
}
