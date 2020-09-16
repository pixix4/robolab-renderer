package de.robolab.client.app.model.file

import de.robolab.client.app.controller.TabController
import de.robolab.client.app.model.base.INavigationBarEntryRoot
import de.robolab.client.app.model.base.INavigationBarList
import de.robolab.client.app.model.file.provider.*
import de.robolab.client.net.IRobolabServer
import de.robolab.client.net.requests.getExamInfo
import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.event.subscribe
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FileNavigationRoot(
    private val tabController: TabController
) : INavigationBarEntryRoot {

    private fun firstRemoteServer(): IRobolabServer? {
        return fileLoaderProperty.value
            .filterIsInstance<RemoteFilePlanetLoader>()
            .firstOrNull()?.server
    }

    val fileLoaderProperty = PreferenceStorage
        .fileServerProperty.mapBinding { list ->
            list.mapNotNull { uri ->
                val protocol = uri.substringBefore("://")
                val factory = loaderFactoryList.find { it.protocol.equals(protocol, true) }
                factory?.create(uri)
            }
        }

    override val searchProperty = property("")

    private val activeListProperty = property<INavigationBarList>(FileRemoteNavigationList(this))
    val activeList by activeListProperty

    override val childrenProperty = activeListProperty.mapBinding { it.childrenProperty }

    override val parentNameProperty = activeListProperty.flatMapBinding {
        it.parentNameProperty
    }

    override fun openParent() {
        activeList.openParent()
    }

    fun openRemoteList() {
        activeListProperty.value = FileRemoteNavigationList(this)
    }

    fun <T : IFilePlanetIdentifier> openRemoteEntryList(
        loader: IFilePlanetLoader<T>,
        entry: T? = null,
        parents: List<T> = emptyList()
    ) {
        activeListProperty.value = FileEntryNavigationList(this, loader, entry, parents)
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
        val loaderFactoryList = getFilePlanetLoaderFactoryList() + listOf(
            RemoteFilePlanetLoader.HttpsFactory,
            RemoteFilePlanetLoader.HttpFactory
        )
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
