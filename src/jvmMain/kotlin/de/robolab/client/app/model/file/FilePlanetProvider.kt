package de.robolab.client.app.model.file

import de.robolab.client.app.model.IProvider
import de.robolab.client.app.model.ISideBarEntry
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableMutableList
import de.westermann.kobserve.list.mapObservable
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sortByObservable
import de.westermann.kobserve.property.property
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

actual class FilePlanetProvider actual constructor() : IProvider {

    override val searchStringProperty = property("")

    actual val planetList: ObservableMutableList<FilePlanetEntry> = observableListOf()
    override val entryList: ObservableList<ISideBarEntry> = planetList
            .sortByObservable { it.titleProperty.value.toLowerCase() }
            .mapObservable { it as ISideBarEntry }

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
        if (Files.exists(dir)) {
            Files.list(dir).forEach {
                loadFile(it)
            }
        }
    }
}
