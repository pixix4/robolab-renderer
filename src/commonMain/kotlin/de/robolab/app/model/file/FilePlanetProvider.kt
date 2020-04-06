package de.robolab.app.model.file

import de.robolab.app.model.IPlottable
import de.westermann.kobserve.list.ObservableList

expect class FilePlanetProvider() {

    val planetList: ObservableList<IPlottable>
}
