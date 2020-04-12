package de.robolab.app.model.file

import de.robolab.app.model.IProvider
import de.robolab.app.model.ISideBarEntry
import de.robolab.app.model.ISideBarPlottable
import de.westermann.kobserve.list.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

actual class FilePlanetProvider actual constructor() : IProvider {

    val planetList: ObservableList<ISideBarPlottable> = observableListOf()
    override val entryList: ObservableReadOnlyList<ISideBarEntry> = planetList.sortObservable(compareBy { it.titleProperty.value.toLowerCase() }).mapObservable { it as ISideBarEntry }

    actual fun loadEntry(entry: FilePlanetEntry, onFinish: (String?) -> Unit) {
        try {
            val file = Paths.get(entry.filename)
            val content = Files.readString(file)
            onFinish(content)
        } catch (e: Exception) {
            onFinish(null)
        }
    }

    actual fun saveEntry(entry: FilePlanetEntry, onFinish: (Boolean) -> Unit) {
        try {
            val file = Paths.get(entry.filename)
            Files.writeString(file, entry.content)
            onFinish(true)
        } catch (e: Exception) {
            onFinish(false)
        }
    }

    private fun loadFile(file: Path) {
        planetList += FilePlanetEntry(file.toString(), this)
    }

    init {
        val dir = Paths.get("planet")
        Files.list(dir).forEach {
            loadFile(it)
        }
    }
}
