package de.robolab.app.model.file

import de.robolab.app.model.IPlottable
import de.westermann.kobserve.list.ObservableList
import de.westermann.kobserve.list.ObservableReadOnlyList
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sortObservable
import kotlin.browser.window

actual class FilePlanetProvider actual constructor() {
    actual val planetList: ObservableList<IPlottable> = observableListOf()
    actual val sortedPlanetList: ObservableReadOnlyList<IPlottable> = planetList.sortObservable(compareBy { it.nameProperty.value.toLowerCase() })

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
