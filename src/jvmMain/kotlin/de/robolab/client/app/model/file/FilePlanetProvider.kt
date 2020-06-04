package de.robolab.client.app.model.file

import de.robolab.client.app.model.IProvider
import de.robolab.client.app.model.INavigationBarEntry
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableMutableList
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sortByObservable
import de.westermann.kobserve.property.property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

actual class FilePlanetProvider actual constructor() : IProvider {

    override val searchStringProperty = property("")

    actual val planetList: ObservableMutableList<FilePlanetEntry> = observableListOf()
    override val entryList: ObservableList<INavigationBarEntry> = planetList
            .sortByObservable { it.titleProperty.value.toLowerCase() }

    actual suspend fun loadEntry(entry: FilePlanetEntry): String? {
        return withContext(Dispatchers.IO) {
            try {
                val file = Paths.get(entry.filename)
                file.toFile().readText()
            } catch (e: Exception) {
                null
            }
        }
    }

    actual suspend fun saveEntry(entry: FilePlanetEntry): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val file = Paths.get(entry.filename)
                file.toFile().writeText(entry.content)
                true
            } catch (e: Exception) {
                false
            }
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
