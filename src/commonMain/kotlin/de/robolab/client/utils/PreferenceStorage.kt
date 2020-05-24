package de.robolab.client.utils

import de.robolab.client.app.controller.SideBarController
import de.robolab.client.renderer.drawable.edit.EditPaperBackground
import de.robolab.client.theme.Theme
import de.robolab.common.utils.KeyValueStorage
import de.robolab.common.utils.Logger
import de.robolab.common.utils.TypedStorage
import de.westermann.kobserve.Binding
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.event.EventHandler
import kotlin.random.Random

object PreferenceStorage: TypedStorage() {

    val selectedThemeProperty = item("THEME", Theme.DEFAULT)
    var selectedTheme by selectedThemeProperty

    val useSystemThemeProperty = item("USE_SYSTEM_THEME", true)
    var useSystemTheme by useSystemThemeProperty

    val exportScaleProperty = item("EXPORT_SCALE", 4.0)
    var exportScale by exportScaleProperty

    val debugModeProperty = item("DEBUG_MODE", false)
    var debugMode by debugModeProperty

    val selectedSideBarTabProperty = item("SIDE_BAR_TAB", SideBarController.Tab.FILE)
    var selectedSideBarTab by selectedSideBarTabProperty

    val clientIdProperty = item("CLIENT_ID", Random.nextBytes(16).joinToString("") { it.toString(16).padStart(2, '0') })
    var clientId by clientIdProperty

    val serverUriProperty = item("SERVER_URI", PlatformDefaultPreferences.serverUriProperty)
    var serverUri by serverUriProperty

    val usernameProperty = item("USERNAME", "")
    var username by usernameProperty

    val passwordProperty = item("PASSWORD", "")
    var password by passwordProperty

    val logLevelProperty = item("LOG_LEVEL", Logger.Level.WARN)
    val logLevel by logLevelProperty


    val paperBackgroundEnabledProperty = item("PAPER_BACKGROUND_ENABLED", false)
    val paperBackgroundEnabled by paperBackgroundEnabledProperty

    val paperGridWidthProperty = item("PAPER_GRID_WIDTH", 0.5)
    val paperGridWidth by paperGridWidthProperty

    val paperStripWidthProperty = item("PAPER_STRIP_WIDTH", 0.841)
    val paperStripWidth by paperStripWidthProperty

    val paperOrientationProperty = item("PAPER_ORIENTATION", EditPaperBackground.Orientation.VERTICAL)
    val paperOrientation by paperOrientationProperty

    val paperMinimalPaddingProperty = item("PAPER_MINIMAL_PADDING", 0.25)
    val paperMinimalPadding by paperMinimalPaddingProperty

    val paperPrecisionProperty = item("PAPER_PRECISION", 3)
    val paperPrecision by paperPrecisionProperty
}

expect object PlatformDefaultPreferences {
    val serverUriProperty: String
}
