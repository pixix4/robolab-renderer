package de.robolab.app.model.file

import de.robolab.app.model.IPlottable
import de.westermann.kobserve.list.ObservableList
import de.westermann.kobserve.list.ObservableReadOnlyList

expect class FilePlanetProvider() {

    val planetList: ObservableList<IPlottable>
    val sortedPlanetList: ObservableReadOnlyList<IPlottable>
}
