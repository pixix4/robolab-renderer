package de.robolab.client.app.model.file

import de.robolab.client.app.model.base.IPlanetProvider
import de.robolab.client.app.model.file.provider.FilePlanetProvider
import de.robolab.client.app.model.file.provider.IFilePlanetLoaderFactory
import de.robolab.client.app.model.file.provider.RemoteFilePlanetLoader
import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.list.asObservable
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class MultiFilePlanetProvider : IPlanetProvider {

    override val searchStringProperty = property("")
    override val entryList = PreferenceStorage
        .fileServerProperty.mapBinding { list ->
            list.mapNotNull { uri ->
                val protocol = uri.substringBefore("://")
                val factory = loaderFactoryList.find { it.protocol.equals(protocol, true) }
                factory?.create(uri)
            }.map { FilePlanetProvider(it) }.toMutableList().asObservable()
        }

    companion object {
        val loaderFactoryList = getFilePlanetLoaderFactoryList() + listOf(
            RemoteFilePlanetLoader.HttpsFactory,
            RemoteFilePlanetLoader.HttpFactory
        )
    }
}

expect fun getFilePlanetLoaderFactoryList(): List<IFilePlanetLoaderFactory>

fun MultiFilePlanetProvider.findByName(name: String): FilePlanetEntry? {
    if (name.isEmpty()) return null

    for (provider in entryList.value) {
        val found = provider.sortedEntries.filter {
            it.titleProperty.value.contains(name, true)
        }.minByOrNull { it.titleProperty.value.length }

        if (found != null && found is FilePlanetEntry) {
            return found
        }
    }

    return null
}
