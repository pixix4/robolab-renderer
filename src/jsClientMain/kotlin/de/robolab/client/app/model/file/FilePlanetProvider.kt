package de.robolab.client.app.model.file

import de.robolab.client.app.model.INavigationBarEntry
import de.robolab.client.app.model.IProvider
import de.robolab.client.net.http
import de.robolab.client.net.web
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableMutableList
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sortByObservable
import de.westermann.kobserve.property.property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.list
import kotlinx.serialization.builtins.serializer

actual class FilePlanetProvider actual constructor() : IProvider {

    override val searchStringProperty = property("")

    actual val planetList: ObservableMutableList<FilePlanetEntry> = observableListOf()
    override val entryList: ObservableList<INavigationBarEntry> = planetList
        .sortByObservable { it.titleProperty.value.toLowerCase() }

    actual suspend fun loadEntry(entry: FilePlanetEntry): String? {
        return http {
            web(entry.filename)
        }.exec().body
    }

    actual suspend fun saveEntry(entry: FilePlanetEntry): Boolean {
        return false
    }

    private suspend fun loadPlanet(name: String) {
        val filename = "/planet/$name"
        val fileEntry = FilePlanetEntry(filename, this)

        withContext(Dispatchers.Main) {
            planetList += fileEntry
        }
    }

    private suspend fun loadPlanetList(planets: List<String>) {
        for (file in planets) {
            loadPlanet(file)
        }
    }

    init {
        GlobalScope.launch(Dispatchers.Default) {
            val response = http {
                web("/planets")
            }.exec().parse(String.serializer().list)

            if (response != null) {
                loadPlanetList(response)
            }
        }
    }
}
