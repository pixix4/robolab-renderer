package de.robolab.app.model.file

import de.robolab.app.model.IPlottable
import de.westermann.kobserve.list.ObservableList
import de.westermann.kobserve.list.observableListOf
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

actual class FilePlanetProvider actual constructor() {
    actual val planetList: ObservableList<IPlottable> = observableListOf()

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
