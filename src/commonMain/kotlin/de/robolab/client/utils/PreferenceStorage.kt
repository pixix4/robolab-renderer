package de.robolab.client.utils

import de.robolab.client.app.controller.SystemController
import de.robolab.client.app.model.file.LoadRemoteExamStateEvent
import de.robolab.client.renderer.drawable.edit.PaperBackgroundDrawable
import de.robolab.client.theme.utils.Theme
import de.robolab.common.utils.Logger
import de.robolab.common.utils.TypedStorage
import de.westermann.kobserve.event.emit
import de.westermann.kobserve.event.now
import kotlin.random.Random

object PreferenceStorage : TypedStorage() {

    val firstStartProperty = item("firstStart", true)
    var firstStart by firstStartProperty


    val logLevelProperty = item("logLevel", Logger.Level.WARN)
    val logLevel by logLevelProperty


    val debugStatusProperty = item("renderer.debugStatus", false)
    var debugStatus by debugStatusProperty

    val debugHierarchyProperty = item("renderer.debugHierarchy", false)
    var debugHierarchy by debugHierarchyProperty


    val exportScaleProperty = item("plotting.exportScale", 4.0)
    var exportScale by exportScaleProperty

    val animationTimeProperty = item("plotting.animationTime", 1000.0)
    var animationTime by animationTimeProperty

    val renderSenderGroupingProperty = item("plotting.senderGrouping", true)
    var renderSenderGrouping by renderSenderGroupingProperty

    val renderAutoScalingProperty = item("plotting.autoScaling", true)
    var renderAutoScaling by renderAutoScalingProperty


    val traverserAutoExpandProperty = item("traverser.autoExpand", false)
    var traverserAutoExpand by traverserAutoExpandProperty

    val traverserDelayProperty = item("traverser.delay", 1)
    var traverserDelay by traverserDelayProperty


    val selectedThemeProperty = item("ui.theme", Theme.DEFAULT)
    var selectedTheme by selectedThemeProperty

    val useSystemThemeProperty = item("ui.useSystemTheme", true)
    var useSystemTheme by useSystemThemeProperty

    val selectedNavigationBarTabProperty = item("ui.selectedNavigationBarTabIndex", 0)
    var selectedNavigationBarTab by selectedNavigationBarTabProperty

    val hideEmptyTabBarProperty = item("ui.hideEmptyTabBar", true)
    var hideEmptyTabBar by hideEmptyTabBarProperty

    val infoBarEnabledProperty = item("ui.infoBarEnabledProperty", true)

    val terminalEnabledProperty = item("ui.terminalEnabledProperty", false)


    val clientIdProperty =
        item("communication.clientId", Random.nextBytes(16).joinToString("") { it.toString(16).padStart(2, '0') })
    var clientId by clientIdProperty

    val serverUriProperty = item("communication.serverUri", PlatformDefaultPreferences.mqttServerUri)
    var serverUri by serverUriProperty

    val usernameProperty = item("communication.username", "")
    var username by usernameProperty

    val passwordProperty = item("communication.password", "")
    var password by passwordProperty

    val authenticationTokenProperty = item("authentication.token", "")
    var authenticationToken by authenticationTokenProperty

    val logUriProperty =
        item("communication.logUri", "https://mothership.inf.tu-dresden.de/logs/mqtt/latest")
    var logUri by logUriProperty

    val logCountProperty =
        item("communication.logCount", 1000)
    var logCount by logCountProperty

    val mqttStorageProperty = item("communication.mqttStorage", MqttStorage.IN_MEMORY)
    var mqttStorage by mqttStorageProperty


    val remoteFilesProperty = item("remote.files", PlatformDefaultPreferences.fileServer)
    var remoteFiles by remoteFilesProperty

    val remoteServerUrlProperty = item("remote.url", PlatformDefaultPreferences.remoteServerUri)
    var remoteServerUrl by remoteServerUrlProperty

    val remoteServerTokenProperty = item("remote.token", "")
    var remoteServerToken by remoteServerTokenProperty


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

    val examPlanetsProperty = item("exam.planets", "")
    var examPlanets by examPlanetsProperty

    init {
        logLevelProperty.onChange.now {
            Logger.level = logLevel
        }

        val fixedRemoteUrl = SystemController.fixedRemoteUrl
        if (fixedRemoteUrl != null) {
            remoteServerUrl = fixedRemoteUrl
            remoteServerUrlProperty.onChange {
                throw IllegalStateException("Cannot change remoteServerUrl cause client was started with fixedRemoteUrl!")
            }
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
    val mqttServerUri: String
    val remoteServerUri: String
    val fileServer: String
}
