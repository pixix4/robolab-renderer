package de.robolab.client.app.controller.ui

import de.robolab.client.repl.*
import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.and
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.not
import de.westermann.kobserve.property.property
import de.westermann.kobserve.property.readOnly
import de.westermann.kobserve.toggle
import kotlin.math.max

class UiController {

    val fullscreenProperty = property(false)

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

    val terminalEnabledProperty = PreferenceStorage.terminalEnabledProperty
    private val terminalHeightMutableProperty = property(300.0)
    val terminalHeightProperty = terminalHeightMutableProperty.readOnly()

    fun setTerminalHeight(height: Double, ignoreWidthToggle: Boolean = false) {
        updateBarWidth(height, terminalHeightMutableProperty, terminalEnabledProperty, ignoreWidthToggle)
    }

    val toolBarVisibleProperty = !fullscreenProperty
    val navigationBarVisibleProperty = !fullscreenProperty and navigationBarEnabledProperty
    val infoBarVisibleProperty = !fullscreenProperty and infoBarEnabledProperty
    val statusBarVisibleProperty = !fullscreenProperty

    private fun updateBarWidth(
        width: Double,
        barWidthProperty: ObservableProperty<Double>,
        barEnabledProperty: ObservableProperty<Boolean>,
        ignoreWidthToggle: Boolean,
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


    init {
        ReplRootCommand.node("ui", "Update general state of the user interface") {
            node("toggle", "Toggle visibility of user interface elements") {
                action(
                    "navigation-bar",
                    "Toggle the left navigation bar",
                    BooleanParameter.param("force", true)
                ) { _, params ->
                    val force = params.firstOrNull() as BooleanParameter?

                    if (force == null) {
                        navigationBarEnabledProperty.toggle()
                    } else {
                        navigationBarEnabledProperty.value = force.value
                    }
                }
                action(
                    "info-bar",
                    "Toggle the right information bar",
                    BooleanParameter.param("force", true)
                ) { _, params ->
                    val force = params.firstOrNull() as BooleanParameter?

                    if (force == null) {
                        infoBarEnabledProperty.toggle()
                    } else {
                        infoBarEnabledProperty.value = force.value
                    }
                }
                action(
                    "terminal",
                    "Toggle the terminal",
                    BooleanParameter.param("force", true)
                ) { _, params ->
                    val force = params.firstOrNull() as BooleanParameter?

                    if (force == null) {
                        terminalEnabledProperty.toggle()
                    } else {
                        terminalEnabledProperty.value = force.value
                    }
                }
                action(
                    "fullscreen",
                    "Toggle fullscreen mode",
                    BooleanParameter.param("force", true)
                ) { _, params ->
                    val force = params.firstOrNull() as BooleanParameter?

                    if (force == null) {
                        fullscreenProperty.toggle()
                    } else {
                        fullscreenProperty.value = force.value
                    }
                }
            }
        }
    }
}
