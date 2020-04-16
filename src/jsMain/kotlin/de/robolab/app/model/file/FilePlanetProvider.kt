package de.robolab.app.model.file

import de.robolab.app.model.IProvider
import de.robolab.app.model.ISideBarEntry
import de.westermann.kobserve.list.*
import de.westermann.kobserve.property.property
import kotlin.browser.window

actual class FilePlanetProvider actual constructor(): IProvider {

    override val searchStringProperty = property("")

    val planetList: ObservableList<FilePlanetEntry> = observableListOf()
    override val entryList: ObservableReadOnlyList<ISideBarEntry> = planetList.sortObservable(compareBy { it.titleProperty.value.toLowerCase() }).mapObservable { it as ISideBarEntry }

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
