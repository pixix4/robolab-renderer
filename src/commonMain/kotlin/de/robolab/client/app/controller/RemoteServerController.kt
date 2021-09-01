package de.robolab.client.app.controller

import de.robolab.client.app.model.file.provider.IFilePlanetLoader
import de.robolab.client.app.model.file.provider.RemoteFilePlanetLoader
import de.robolab.client.net.PingRobolabServer
import de.robolab.client.net.RESTRobolabServer
import de.robolab.client.net.requests.getVersion
import de.robolab.client.net.requests.info.getExamInfo
import de.robolab.client.net.requests.info.whoami
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.net.headers.AuthorizationHeader
import de.robolab.common.utils.Logger
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.now
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
import de.westermann.kobserve.property.property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext

class RemoteServerController() {

    private val logger = Logger(this)

    val remoteServerUriProperty = PreferenceStorage.remoteServerUrlProperty.mapBinding { url ->
        if (url.isBlank() || url.contains("file://")) null else url
    }

    private var lastPingServer: PingRobolabServer? = null
    val remoteServerProperty = remoteServerUriProperty.mapBinding { uri ->
        if (uri == null) null else {
            val host = uri.substringAfter("://").trimEnd('/')
            lastPingServer?.stopPing()
            val restServer = RESTRobolabServer(host, 0, !uri.startsWith("http://"))

            if (PreferenceStorage.authenticationToken.isNotEmpty()) {
                try {
                    restServer.authHeader = AuthorizationHeader.Bearer(PreferenceStorage.authenticationToken)
                } catch (e: Exception) {
                    logger.error(e)
                }
            }

            val pingServer = PingRobolabServer(restServer)
            pingServer.startPing()
            lastPingServer = pingServer
            pingServer
        }
    }
    val remoteServer by remoteServerProperty

    val remoteServerVersionProperty = property("")
    val remoteServerAuthenticationProperty = property("")

    val remotePlanetLoaderProperty: ObservableValue<IFilePlanetLoader?> = remoteServerProperty.mapBinding {
        if (it == null) null else RemoteFilePlanetLoader(it)
    }

    private val updateServerStateMutex = Mutex()
    private fun updateServerState() {
        if (updateServerStateMutex.tryLock()) {

            val server = remoteServer
            if (server == null) {
                remoteServerVersionProperty.value = ""
                remoteServerAuthenticationProperty.value = ""
            } else {
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        remoteServerVersionProperty.value = "Loading…"
                        remoteServerAuthenticationProperty.value = "Loading…"
                    }
                    try {
                        val version = server.getVersion().okOrThrow().version
                        withContext(Dispatchers.Main) {
                            remoteServerVersionProperty.value = version.toString()
                        }
                    } catch (e: Throwable) {
                        withContext(Dispatchers.Main) {
                            remoteServerVersionProperty.value = "Cannot load server version!"
                        }
                    }

                    try {
                        val user = server.whoami().okOrThrow().user
                        withContext(Dispatchers.Main) {
                            remoteServerAuthenticationProperty.value = "${user.username} (${user.accessLevel.name})"
                        }
                    } catch (e: Throwable) {
                        withContext(Dispatchers.Main) {
                            remoteServerAuthenticationProperty.value = "Cannot load auth information!"
                        }
                    }
                }
            }

            updateServerStateMutex.unlock()
        }
    }

    fun loadRemoteExamState() {
        if (!PreferenceStorage.useRemoteExamState) return
        val server = remoteServer ?: return

        GlobalScope.launch {
            try {
                val info = server.getExamInfo().okOrThrow()

                PreferenceStorage.examActive = info.isExam
                if (info.isExam) {
                    PreferenceStorage.examPlanets = info.planets.joinToString(";") { "${it.name}=${it.info.name}" }
                }
            } catch (e: Exception) {

            }
        }
    }

    init {
        remoteServerProperty.onChange.now {
            updateServerState()
        }
        remoteServerProperty.nullableFlatMapBinding { it?.authHeaderProperty }.onChange {
            updateServerState()
        }

        loadRemoteExamState()
    }
}
