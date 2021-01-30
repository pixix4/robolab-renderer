package de.robolab.client.ui.views

import de.robolab.client.app.controller.InfoBarController
import de.robolab.client.app.controller.UiController
import de.robolab.client.app.model.file.details.*
import de.robolab.client.app.model.group.InfoBarGroupMessages
import de.robolab.client.app.model.room.InfoBarRoomRobots
import de.robolab.client.ui.views.boxes.*
import de.robolab.common.utils.Point
import de.westermann.kobserve.property.mapBinding
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.clientPosition
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.iconView

class InfoBar(
    private val infoBarController: InfoBarController,
    private val uiController: UiController
) : ViewCollection<View>() {

    @Suppress("JoinDeclarationAndAssignment")
    private val headerView: BoxView

    @Suppress("JoinDeclarationAndAssignment")
    private val contentView: BoxView

    private fun updateHeader() {
        headerView.clear()

        val tabs = infoBarController.infoBarTabsProperty.value ?: return

        for (tab in tabs) {
            headerView.boxView("tab-bar-item") {
                classList.bind("active", infoBarController.infoBarActiveTabProperty.mapBinding { it == tab })

                iconView(tab.icon)
                title = tab.tooltip

                onClick {
                    tab.open()
                }
            }
        }
    }

    private fun updateContent() {
        contentView.clear()

        val content = infoBarController.infoBarContentProperty.value ?: return

        when (content) {
            is InfoBarFileView -> {
                contentView.add(InfoBarFileViewView(content, uiController))
            }
            is InfoBarFilePaper -> {
                contentView.add(InfoBarFilePaperView(content, uiController))
            }
            is InfoBarFileEdit -> {
                contentView.add(InfoBarFileEditView(content, uiController))
            }
            is InfoBarFileTraverse -> {
                if (content.traverserProperty.value == null) {
                    content.traverse()
                }
                contentView.add(NullableViewContainer(content.traverserProperty) {
                    InfoBarFileTraverseView(it, uiController)
                })
            }
            is InfoBarFileTest -> {
                if (content.testProperty.value == null) {
                    content.test()
                }
                contentView.add(NullableViewContainer(content.testProperty) {
                    InfoBarFileTestView(it, uiController)
                })
            }
            is InfoBarGroupMessages -> {
                contentView.add(InfoBarGroupMessagesView(content, uiController))
            }
            is InfoBarRoomRobots -> {
                contentView.add(InfoBarRoomRobotsView(content, uiController))
            }
        }
    }

    init {
        headerView = boxView("info-bar-header") {}
        contentView = boxView("info-bar-content") {}

        infoBarController.infoBarTabsProperty.onChange {
            updateHeader()
        }
        updateHeader()

        infoBarController.infoBarContentProperty.onChange {
            updateContent()
        }
        updateContent()

        // Close info bar on mobile
        var closePosition = Point.ZERO
        onMouseDown {
            closePosition = it.clientPosition
        }
        onClick {
            if (it.target == html && it.clientPosition.distanceTo(closePosition) < 2.0 && uiController.infoBarEnabledProperty.value) {
                uiController.infoBarEnabledProperty.value = false
                it.preventDefault()
                it.stopPropagation()
            }
        }
    }
}
