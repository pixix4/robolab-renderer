package de.robolab.client.app.model.file

import de.robolab.client.app.controller.TabController
import de.robolab.client.app.model.file.provider.*
import de.robolab.client.net.IRobolabServer
import de.robolab.client.net.requests.getVersion
import de.robolab.client.net.requests.info.getExamInfo
import de.robolab.client.net.requests.info.whoami
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.net.headers.AuthorizationHeader
import de.robolab.common.utils.Logger
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.subscribe
import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
import de.westermann.kobserve.property.property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FileNavigationManager(
    private val tabController: TabController
) {

    val remoteServer: IRobolabServer?
        get() {
            return remotePlanetLoader.value?.server
        }

    private val remotePlanetLoader: ObservableValue<RemoteFilePlanetLoader?> =
        PreferenceStorage.remoteServerUrlProperty.mapBinding {
            val loader = if (it.isBlank()) {
                null
            } else {
                RemoteFilePlanetLoader.create(it)
            }

            if (PreferenceStorage.authenticationToken.isNotEmpty()) {
                try {
                    loader?.server?.authHeader = AuthorizationHeader.Bearer(PreferenceStorage.authenticationToken)
                } catch (e: Exception) {
                    logger.error(e)
                }
            }

            loader
        }

    val remoteServerVersionProperty = property("")
    val remoteServerAuthenticationProperty = property("")

    val fileLoaderProperty = PreferenceStorage
        .remoteFilesProperty.join(remotePlanetLoader) { list, remote ->
            listOfNotNull(
                remote
            ) + list.mapNotNull { uri ->
                loaderFactoryList.asSequence().mapNotNull { it.create(uri) }.firstOrNull()
            }
        }

    val fileNavigationList =
        fileLoaderProperty.mapBinding { list ->
            list.map {
                FileNavigationTab(this, it)
            }
        }

    fun <T : IFilePlanetIdentifier> openFileEntry(loader: IFilePlanetLoader<T>, entry: T, asNewTab: Boolean) {
        tabController.open(FilePlanetDocument(FilePlanet(loader, entry)), asNewTab)
    }

    private fun loadRemoteExamState() {
        if (!PreferenceStorage.useRemoteExamState) return
        val server = remoteServer ?: return

        GlobalScope.launch {
            try {
                val info = server.getExamInfo().okOrThrow()

                PreferenceStorage.examActive = info.isExam
                if (info.isExam) {
                    PreferenceStorage.examSmall = info.smallInfo?.name ?: ""
                    PreferenceStorage.examLarge = info.largeInfo?.name ?: ""
                }
            } catch (e: Exception) {

            }
        }
    }

    init {
        subscribe<LoadRemoteExamStateEvent> {
            loadRemoteExamState()
            updateServerState()
        }

        remotePlanetLoader.onChange {
            updateServerState()
        }
        remotePlanetLoader.nullableFlatMapBinding { it?.server?.authHeaderProperty }.onChange {
            updateServerState()
        }

        loadRemoteExamState()
        updateServerState()
    }

    private fun updateServerState() {
        val server = remoteServer

        if (server == null) {
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    remoteServerVersionProperty.value = ""
                    remoteServerAuthenticationProperty.value = ""
                }
            }
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
                    logger.error("Load server version", e)
                    withContext(Dispatchers.Main) {
                        remoteServerVersionProperty.value = "Cannot find server!"
                    }
                }

                try {
                    val user = server.whoami().okOrThrow().user
                    withContext(Dispatchers.Main) {
                        remoteServerAuthenticationProperty.value = "${user.username} (${user.accessLevel.name})"
                    }
                } catch (e: Throwable) {
                    logger.error("Load server who am i", e)
                    withContext(Dispatchers.Main) {
                        remoteServerAuthenticationProperty.value = "Cannot find server!"
                    }
                }
            }
        }
    }

    companion object {
        private val logger = Logger("FileNavigationRoot")
        val loaderFactoryList = getFilePlanetLoaderFactoryList()
    }
}

object LoadRemoteExamStateEvent

expect fun getFilePlanetLoaderFactoryList(): List<IFilePlanetLoaderFactory>

/**
 * Instructs the client to request a new authentication token for the specified server.
 *
 * @param server Server for the token request
 * @param userConfirm Show request confirm dialog to the user
 *
 * @return `true` if the user
 */
expect suspend fun requestAuthToken(server: IRobolabServer, userConfirm: Boolean): Boolean

suspend fun <T : IFilePlanetIdentifier> IFilePlanetLoader<T>.searchPlanet(name: String): FilePlanet<T>? {
    val entry = searchPlanets(name, true).firstOrNull() ?: return null
    return FilePlanet(this, entry)
}

suspend fun FileNavigationManager.searchPlanet(name: String): FilePlanet<*>? {
    if (name.isEmpty()) return null

    for (loader in fileLoaderProperty.value) {
        val entry = loader.searchPlanet(name)
        if (entry != null) {
            return entry
        }
    }

    return null
}

suspend fun FileNavigationManager.requestAuthToken(userConfirm: Boolean): Boolean {
    val server = remoteServer ?: return false
    return requestAuthToken(server, userConfirm)
}
