package de.robolab.client.app.controller.dialog

import de.robolab.client.app.controller.RemoteServerController
import de.robolab.client.app.controller.SystemController
import de.robolab.client.net.requests.mqtt.GetMQTTURLs
import de.robolab.client.net.requests.mqtt.getMQTTCredentials
import de.robolab.client.net.requests.mqtt.getMQTTURLs
import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.readOnly
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsDialogController(
    private val remoteServerController: RemoteServerController
) {

    val isDesktop: Boolean = SystemController.isDesktop
    val serverAuthenticationProperty: ObservableValue<String> =
        remoteServerController.remoteServerAuthenticationProperty.readOnly()
    val serverVersionProperty: ObservableValue<String> = remoteServerController.remoteServerVersionProperty.readOnly()

    fun requestAuthToken() {
        val server = remoteServerController.remoteServer
        if (server != null) {
            GlobalScope.launch {
                de.robolab.client.app.model.file.requestAuthToken(server, false)
            }
        }
    }

    fun loadMqttSettings(selectUri: (GetMQTTURLs.MQTTURLsResponse) -> String = { it.wssURL }) {
        val server = remoteServerController.remoteServer
        if (server != null) {
            GlobalScope.launch {
                val credentials = server.getMQTTCredentials().okOrNull() ?: return@launch
                val urls = server.getMQTTURLs().okOrNull() ?: return@launch

                withContext(Dispatchers.Main) {
                    PreferenceStorage.serverUri = selectUri(urls)
                    PreferenceStorage.logUri = urls.logURL
                    PreferenceStorage.username = credentials.credentials.username
                    PreferenceStorage.password = credentials.credentials.password
                }
            }
        }
    }

    fun openDirectory() {
        TODO()
    }
}
