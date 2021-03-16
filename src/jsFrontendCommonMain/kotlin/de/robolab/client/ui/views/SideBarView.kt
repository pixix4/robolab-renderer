package de.robolab.client.ui.views

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.viewmodel.SideBarTabViewModel
import de.robolab.client.app.viewmodel.SideBarViewModel
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.ViewFactoryRegistry
import de.westermann.kobserve.event.now
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.bindView
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.iconView
import de.westermann.kwebview.components.textView
import de.westermann.kwebview.extra.listFactory

class SideBarView(
    private val viewModel: SideBarViewModel
) : ViewCollection<View>() {

    private fun createHeader(tab: SideBarTabViewModel): View {
        return BoxView().apply {
            classList += "tab-bar-view-item"
            classList.bind("active", viewModel.activeTab.mapBinding { it == tab })

            iconView(tab.iconProperty) {
                classList += "tab-bar-view-icon"
            }
            titleProperty.bind(tab.nameProperty)

            onClick {
                viewModel.open(tab)
            }
        }
    }

    private fun createContent(tab: SideBarTabViewModel?): View {
        if (tab == null) return BoxView()

        return BoxView().apply {
            classList+= "side-bar-view-content"
            classList.bind("hide-back-button", tab.contentProperty.mapBinding { it.parent == null })
            classList.bind("hide-top-tool-bar", tab.topToolBar.contentProperty.mapBinding { it.isEmpty() })
            classList.bind("hide-bottom-tool-bar", tab.bottomToolBar.contentProperty.mapBinding { it.isEmpty() })

            boxView {
                +ViewFactoryRegistry.create(tab.topToolBar)
            }
            boxView {
                iconView(MaterialIcon.ARROW_BACK)
                textView(tab.contentProperty.flatMapBinding { it.nameProperty })

                onClick {
                    tab.onNavigateBack()
                }
            }
            boxView {
                bindView(tab.contentProperty) {
                    ViewFactoryRegistry.create(tab.contentProperty.value)
                }
            }
            boxView {
                +ViewFactoryRegistry.create(tab.bottomToolBar)
            }
        }
    }

    init {
        boxView("side-bar-view-header", "tab-bar-view", "tab-bar-view-compact") {
            listFactory(viewModel.tabList, {
                createHeader(it)
            })
        }
        boxView("side-bar-view-container") {
            bindView(viewModel.activeTab) {
                createContent(it)
            }
        }

        // Close navigation bar on mobile
        onClick {
            if (it.target == html) {
                viewModel.closeSideBar()
                it.preventDefault()
                it.stopPropagation()
            }
        }
    }

    companion object : ViewFactory {
        override fun matches(viewModel: ViewModel): Boolean {
            return viewModel is SideBarViewModel
        }

        override fun create(viewModel: ViewModel): View {
            return SideBarView(viewModel as SideBarViewModel)
        }
    }
}
