package de.robolab.app.model.file

import de.robolab.app.model.IProvider
import de.robolab.app.model.ISideBarEntry
import de.westermann.kobserve.list.*
import kotlin.browser.window

actual class FilePlanetProvider actual constructor(): IProvider {

    val planetList: ObservableList<FilePlanetEntry> = observableListOf()
    override val entryList: ObservableReadOnlyList<ISideBarEntry> = planetList.sortObservable(compareBy { it.titleProperty.value.toLowerCase() }).mapObservable { it as ISideBarEntry }

    private fun loadPlanet(name: String) {
        window.fetch("/planet/$name").then {
            it.text()
        }.then { content ->
            planetList += FilePlanetEntry(name, content)
        }
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
