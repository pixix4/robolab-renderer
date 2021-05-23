package de.robolab.client.ui.views

import de.robolab.client.app.controller.ui.ContentSplitController
import de.robolab.client.app.viewmodel.ContentViewModel
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.views.utils.ResizeView
import de.westermann.kobserve.event.now
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.extra.listFactory
import kotlinx.browser.window

class ContentView(
    viewModel: ContentViewModel
) : ViewCollection<View>() {

    private val rootProperty = viewModel.contentController.content.rootProperty
    private var tabViewList = emptyList<ContentTabView>()

    private fun createNode(node: ContentSplitController.Node): View {
        val tabView = ContentTabView(node.content)
        tabView.classList.bind("active", node.activeProperty)
        tabViewList = tabViewList.plusElement(tabView)
        return tabView
    }

    private fun createContainer(container: ContentSplitController.Container): View {
        return BoxView().apply {
            classList += container.orientation.name.lowercase()

            listFactory(container.entryList, {
                createEntry(it)
            })
        }
    }

    private fun createEntry(entry: ContentSplitController.Entry): View {
        return when (entry) {
            is ContentSplitController.Node -> createNode(entry)
            is ContentSplitController.Container -> createContainer(entry)
            else -> BoxView()
        }
    }

    private fun checkSizeChange() {
        var changes = false

        for (tab in tabViewList) {
            if (tab.isAttached) {
                tab.checkSizeChange()
            } else {
                changes = true
            }
        }

        if (changes) {
            tabViewList = tabViewList.filter { it.isAttached }
        }
    }

    init {
        viewModel.contentController.onRender {
            checkSizeChange()
        }

        +ResizeView("navigation-bar-resize") { position, _ ->
            viewModel.setNavigationBarWidth(position.x)
        }

        +ResizeView("info-bar-resize") { position, size ->
            viewModel.setInfoBarWidth(window.innerWidth - position.x - size.x)
        }

        boxView("content-view-root") {
            rootProperty.onChange.now {
                val entry = rootProperty.value
                clear()
                +createEntry(entry)
            }
        }
    }

    companion object : ViewFactory {
        override fun matches(viewModel: ViewModel) = viewModel is ContentViewModel
        override fun create(viewModel: ViewModel) = ContentView(viewModel as ContentViewModel)
    }
}
