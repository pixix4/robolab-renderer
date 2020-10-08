package de.robolab.client.utils

import de.robolab.client.app.controller.NavigationBarController
import de.robolab.client.app.model.file.LoadRemoteExamStateEvent
import de.robolab.client.renderer.drawable.edit.PaperBackgroundDrawable
import de.robolab.client.theme.Theme
import de.robolab.common.utils.Logger
import de.robolab.common.utils.TypedStorage
import de.westermann.kobserve.event.emit
import de.westermann.kobserve.event.now
import kotlin.random.Random

object PreferenceStorage : TypedStorage() {


    val firstStartProperty = item("firstStart", true)
    var firstStart by firstStartProperty


    val debugModeProperty = item("debugMode", false)
    var debugMode by debugModeProperty

    val logLevelProperty = item("logLevel", Logger.Level.WARN)
    val logLevel by logLevelProperty


    val exportScaleProperty = item("plotting.exportScale", 4.0)
    var exportScale by exportScaleProperty

    val animationTimeProperty = item("plotting.animationTime", 1000.0)
    var animationTime by animationTimeProperty

    val renderSenderGroupingProperty = item("plotting.senderGrouping", true)
    var renderSenderGrouping by renderSenderGroupingProperty


    val selectedThemeProperty = item("ui.theme", Theme.DEFAULT)
    var selectedTheme by selectedThemeProperty

    val useSystemThemeProperty = item("ui.useSystemTheme", true)
    var useSystemTheme by useSystemThemeProperty

    val selectedNavigationBarTabProperty = item("ui.selectedNavigationBarTab", NavigationBarController.Tab.GROUP)
    var selectedNavigationBarTab by selectedNavigationBarTabProperty

    val hideEmptyTabBarProperty = item("ui.hideEmptyTabBar", true)
    var hideEmptyTabBar by hideEmptyTabBarProperty

    val infoBarEnabledProperty = item("ui.infoBarEnabledProperty", true)


    val clientIdProperty =
        item("communication.clientId", Random.nextBytes(16).joinToString("") { it.toString(16).padStart(2, '0') })
    var clientId by clientIdProperty

    val serverUriProperty = item("communication.serverUri", PlatformDefaultPreferences.serverUriProperty)
    var serverUri by serverUriProperty

    val usernameProperty = item("communication.username", "")
    var username by usernameProperty

    val passwordProperty = item("communication.password", "")
    var password by passwordProperty

    val logUriProperty =
        item("communication.logUri", "https://mothership.inf.tu-dresden.de/logs/mqtt/latest/?count=100")
    var logUri by logUriProperty

    val fileServerProperty = item("communication.fileServer", PlatformDefaultPreferences.fileServer)
    var fileServer by fileServerProperty

    val mqttStorageProperty = item("communication.mqttStorage", MqttStorage.IN_MEMORY)
    var mqttStorage by mqttStorageProperty


    val paperGridWidthProperty = item("paper.gridWidth", 0.5)
    val paperGridWidth by paperGridWidthProperty

    val paperStripWidthProperty = item("paper.stripWidth", 0.914)
    val paperStripWidth by paperStripWidthProperty

    val paperOrientationProperty = item("paper.orientation", PaperBackgroundDrawable.Orientation.VERTICAL)
    val paperOrientation by paperOrientationProperty

    val paperMinimalPaddingProperty = item("paper.minimalPadding", 0.25)
    val paperMinimalPadding by paperMinimalPaddingProperty

    val paperPrecisionProperty = item("paper.precision", 3)
    val paperPrecision by paperPrecisionProperty


    val autoUpdateChannelProperty = item("update.channel", UpdateChannel.STABLE)
    val autoUpdateChannel by autoUpdateChannelProperty


    val useRemoteExamStateProperty = item("exam.remote", false)
    var useRemoteExamState by useRemoteExamStateProperty

    val examActiveProperty = item("exam.active", false)
    var examActive by examActiveProperty

    val examSmallProperty = item("exam.small", "")
    var examSmall by examSmallProperty

    val examLargeProperty = item("exam.large", "")
    var examLarge by examLargeProperty

    init {
        logLevelProperty.onChange.now {
            Logger.level = logLevel
        }

        useRemoteExamStateProperty.onChange {
            if (useRemoteExamState) {
                emit(LoadRemoteExamStateEvent)
            }
        }
    }
}

enum class UpdateChannel {
    STABLE, NIGHTLY, NEVER
}

enum class MqttStorage {
    IN_MEMORY, DATABASE
}

expect object PlatformDefaultPreferences {
    val serverUriProperty: String
    val fileServer: List<String>
}
