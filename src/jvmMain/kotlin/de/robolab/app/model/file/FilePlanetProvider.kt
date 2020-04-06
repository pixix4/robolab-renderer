package de.robolab.app.model.file

import de.robolab.app.model.IPlottable
import de.westermann.kobserve.list.ObservableList
import de.westermann.kobserve.list.ObservableReadOnlyList
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sortObservable
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

actual class FilePlanetProvider actual constructor() {
    actual val planetList: ObservableList<IPlottable> = observableListOf()
    actual val sortedPlanetList: ObservableReadOnlyList<IPlottable> = planetList.sortObservable(compareBy { it.nameProperty.value.toLowerCase() })

    private fun loadFile(file: Path) {
        val name = file.fileName.toString()
        val content = Files.readString(file)
        planetList += FilePlanetEntry(name, content)
    }

    init {
        val dir = Paths.get("planet")
        Files.list(dir).forEach {
            loadFile(it)
        }
    }
}
