package de.robolab.client.app.model.file

import de.robolab.client.app.model.base.IPlanetProvider
import de.robolab.client.app.model.file.provider.FilePlanet
import de.robolab.client.app.model.file.provider.FilePlanetProvider
import de.robolab.client.app.model.file.provider.IFilePlanetLoaderFactory
import de.robolab.client.app.model.file.provider.RemoteFilePlanetLoader
import de.robolab.client.net.IRobolabServer
import de.robolab.client.net.requests.getExamInfo
import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.event.subscribe
import de.westermann.kobserve.list.asObservable
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MultiFilePlanetProvider : IPlanetProvider {

    private fun firstRemoteServer(): IRobolabServer? {
        return entryList.value
            .map { it.loader }
            .filterIsInstance<RemoteFilePlanetLoader>()
            .firstOrNull()?.server
    }

    override val searchStringProperty = property("")
    override val entryList = PreferenceStorage
        .fileServerProperty.mapBinding { list ->
            list.mapNotNull { uri ->
                val protocol = uri.substringBefore("://")
                val factory = loaderFactoryList.find { it.protocol.equals(protocol, true) }
                factory?.create(uri)
            }.map { FilePlanetProvider(it) }.toMutableList().asObservable()
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

suspend fun MultiFilePlanetProvider.searchPlanet(name: String): FilePlanet<*>? {
    if (name.isEmpty()) return null

    for (provider in entryList.value) {
        val list = provider.searchPlanets(name)
        return list.firstOrNull() ?: continue
    }

    return null
}
