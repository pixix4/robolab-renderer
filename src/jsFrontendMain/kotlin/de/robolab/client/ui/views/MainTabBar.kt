package de.robolab.client.ui.views

import de.robolab.client.app.controller.TabController
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection

class MainTabBar(
    private val tabBarController: TabController
) : ViewCollection<View>() {

    init {
        classList += "tab-bar"
    }
}