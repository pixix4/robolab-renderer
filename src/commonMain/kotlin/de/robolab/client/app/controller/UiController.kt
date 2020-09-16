package de.robolab.client.app.controller

import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.and
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.not
import de.westermann.kobserve.property.property
import de.westermann.kobserve.property.readOnly
import kotlin.math.max

class UiController(
    tabController: TabController
) {

    val fullscreenProperty = tabController.fullscreenProperty

    val navigationBarEnabledProperty = property(true)
    private val navigationBarWidthMutableProperty = property(300.0)
    val navigationBarWidthProperty = navigationBarWidthMutableProperty.readOnly()

    fun setNavigationBarWidth(width: Double, ignoreWidthToggle: Boolean = false) {
        updateBarWidth(width, navigationBarWidthMutableProperty, navigationBarEnabledProperty, ignoreWidthToggle)
    }

    val infoBarEnabledProperty = PreferenceStorage.infoBarEnabledProperty
    private val infoBarWidthMutableProperty = property(300.0)
    val infoBarWidthProperty = infoBarWidthMutableProperty.readOnly()

    fun setInfoBarWidth(width: Double, ignoreWidthToggle: Boolean = false) {
        updateBarWidth(width, infoBarWidthMutableProperty, infoBarEnabledProperty, ignoreWidthToggle)
    }

    val toolBarVisibleProperty = !fullscreenProperty
    val navigationBarVisibleProperty = !fullscreenProperty and navigationBarEnabledProperty
    val infoBarVisibleProperty = !fullscreenProperty and infoBarEnabledProperty
    val statusBarVisibleProperty = !fullscreenProperty

    private fun updateBarWidth(
        width: Double,
        barWidthProperty: ObservableProperty<Double>,
        barEnabledProperty: ObservableProperty<Boolean>,
        ignoreWidthToggle: Boolean
    ) {
        var w = width
        if (barEnabledProperty.value) {
            if (w < 50.0) {
                if (!ignoreWidthToggle) {
                    barEnabledProperty.value = false
                }
            } else {
                w = max(w, 200.0)
                barWidthProperty.value = w
            }
        } else {
            if (w >= 50.0 && !ignoreWidthToggle) {
                barEnabledProperty.value = true
            }
        }
    }
}
