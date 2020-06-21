package de.robolab.client.app.model.file

import de.robolab.client.app.model.base.IPlanetProvider
import de.robolab.client.app.model.file.provider.FilePlanetProvider
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class MultiFilePlanetProvider() : IPlanetProvider {

    val providers = getPlanetProviderList()

//    val selectedProvider = property(providers.firstOrNull())

    override val searchStringProperty = property("")
//    override val entryList = selectedProvider.mapBinding {
//        it?.sortedEntries ?: observableListOf<FilePlanetEntry>()
//    }
    override val entryList = constObservable(providers)
}

expect fun getPlanetProviderList(): ObservableList<FilePlanetProvider<*>>

fun MultiFilePlanetProvider.findByName(name: String): FilePlanetEntry? {
    if (name.isEmpty()) return null

    for (provider in providers) {
        val found = provider.sortedEntries.filter {
            it.titleProperty.value.contains(name, true)
        }.minBy { it.titleProperty.value.length }

        if (found != null) {
            return found
        }
    }

    return null
}
