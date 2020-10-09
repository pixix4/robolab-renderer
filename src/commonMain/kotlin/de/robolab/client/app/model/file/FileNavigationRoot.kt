package de.robolab.client.app.model.file

import de.robolab.client.app.controller.TabController
import de.robolab.client.app.model.file.provider.*
import de.robolab.client.net.IRobolabServer
import de.robolab.client.net.requests.getExamInfo
import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.subscribe
import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.mapBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FileNavigationRoot(
    private val tabController: TabController
) {

    private fun firstRemoteServer(): IRobolabServer? {
        return remotePlanetLoader.value?.server
    }

    private val remotePlanetLoader: ObservableValue<RemoteFilePlanetLoader?> =
        PreferenceStorage.remoteServerUrlProperty.mapBinding {
            if (it.isBlank()) {
                null
            } else {
                if (it.startsWith("https")) {
                    RemoteFilePlanetLoader.HttpsFactory.create(it)
                } else {
                    RemoteFilePlanetLoader.HttpFactory.create(it)
                } as? RemoteFilePlanetLoader
            }
        }

    val fileLoaderProperty = PreferenceStorage
        .remoteFilesProperty.join(remotePlanetLoader) { list, remote ->
            listOfNotNull(
                remote
            ) + list.mapNotNull { uri ->
                loaderFactoryList.firstOrNull()?.create(uri)
            }
        }

    val fileNavigationList =
        fileLoaderProperty.mapBinding { list ->
            list.map {
                FileEntryNavigationRootList(this, it)
            }
        }

    fun <T : IFilePlanetIdentifier> openFileEntry(loader: IFilePlanetLoader<T>, entry: T, asNewTab: Boolean) {
        tabController.open(FileEntryPlanetDocument(FilePlanet(loader, entry)), asNewTab)
    }

    private fun loadRemoteExamState() {
        if (!PreferenceStorage.useRemoteExamState) return
        val server = firstRemoteServer() ?: return

        GlobalScope.launch {
            try {
                val info = server.getExamInfo()

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
        }

        loadRemoteExamState()
    }

    companion object {
        val loaderFactoryList = getFilePlanetLoaderFactoryList()
    }
}

object LoadRemoteExamStateEvent

expect fun getFilePlanetLoaderFactoryList(): List<IFilePlanetLoaderFactory>

suspend fun <T : IFilePlanetIdentifier> IFilePlanetLoader<T>.searchPlanet(name: String): FilePlanet<T>? {
    val entry = searchPlanets(name).firstOrNull() ?: return null
    return FilePlanet(this, entry)
}

suspend fun FileNavigationRoot.searchPlanet(name: String): FilePlanet<*>? {
    if (name.isEmpty()) return null

    for (loader in fileLoaderProperty.value) {
        val entry = loader.searchPlanet(name)
        if (entry != null) {
            return entry
        }
    }

    return null
}
