package de.robolab.app.model.file

import de.robolab.app.model.IProvider
import de.robolab.app.model.ISideBarEntry
import de.robolab.app.model.ISideBarPlottable
import de.westermann.kobserve.list.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

actual class FilePlanetProvider actual constructor(): IProvider {

    val planetList: ObservableList<ISideBarPlottable> = observableListOf()
    override val entryList: ObservableReadOnlyList<ISideBarEntry> = planetList.sortObservable(compareBy { it.titleProperty.value.toLowerCase() }).mapObservable { it as ISideBarEntry }

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
