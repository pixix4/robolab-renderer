package de.robolab.client.app.model.file

import de.robolab.client.app.model.IProvider
import de.robolab.client.app.model.INavigationBarEntry
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableMutableList
import de.westermann.kobserve.list.mapObservable
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sortByObservable
import de.westermann.kobserve.property.property
import kotlin.browser.window

actual class FilePlanetProvider actual constructor() : IProvider {

    override val searchStringProperty = property("")

    actual val planetList: ObservableMutableList<FilePlanetEntry> = observableListOf()
    override val entryList: ObservableList<INavigationBarEntry> = planetList
            .sortByObservable { it.titleProperty.value.toLowerCase() }
            .mapObservable { it as INavigationBarEntry }

    actual fun loadEntry(entry: FilePlanetEntry, onFinish: (String?) -> Unit) {
        window.fetch(entry.filename).then {
            it.text()
        }.then { content ->
            onFinish(content)
        }.catch {
            onFinish(null)
        }
    }

    actual fun saveEntry(entry: FilePlanetEntry, onFinish: (Boolean) -> Unit) {
        onFinish(false)
    }

    private fun loadPlanet(name: String) {
        val filename = "/planet/$name"
        planetList += FilePlanetEntry(filename, this)
    }

    private fun loadPlanetList(planets: List<String>) {
        for (file in planets) {
            loadPlanet(file)
        }
    }

    init {
        window.fetch("/planets").then {
            it.json()
        }.then { list ->
            if (list is Array<*>) {
                loadPlanetList(list.filterIsInstance<String>())
            }
        }
    }
}
