package de.robolab.client.app.model.file.provider

import de.robolab.client.app.model.base.INavigationBarGroup
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.FilePlanetEntry
import de.westermann.kobserve.base.ObservableMutableList
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.list.mapObservable
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sortByObservable
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.mapBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FilePlanetProvider<T : IFilePlanetIdentifier>(
    private val loader: IFilePlanetLoader<T>
) : INavigationBarGroup {

    val name: String
        get() = "${loader.name} (${loader.desc})"

    val icon: MaterialIcon
        get() = loader.icon

    private val planetList: ObservableMutableList<FilePlanet<T>> = observableListOf()

    val sortedEntries = planetList
        .mapObservable { FilePlanetEntry(it) }
        .sortByObservable { it.titleProperty.value.toLowerCase() }

    override val entryList = constObservable(sortedEntries)

    override val titleProperty: ObservableValue<String> = constObservable(name)
    override val subtitleProperty: ObservableValue<String> = planetList.mapBinding { "${it.size} entries" }
    override val tabNameProperty: ObservableValue<String> = constObservable(name)
    override val hasContextMenu: Boolean = false
    override val statusIconProperty: ObservableValue<List<MaterialIcon>> = constObservable(listOf(icon))
    override val parent: INavigationBarGroup? = null

    private var oldIdentifierList = emptyList<T>()

    private suspend fun addFile(file: FilePlanet<T>) = withContext(Dispatchers.Main) {
        planetList += file
    }

    private suspend fun removeFile(file: FilePlanet<T>) = withContext(Dispatchers.Main) {
        planetList -= file
    }

    suspend fun sync() {
        val newIdentifierList = loader.loadIdentifierList()
        if (newIdentifierList == oldIdentifierList) {
            return
        }

        val currentList = planetList.toList()
        val referenceList = newIdentifierList.toMutableList()


        for (file in currentList) {
            val identifier = referenceList.find {
                it == file.localIdentifier || it == file.remoteIdentifier
            }

            if (identifier == null) {
                removeFile(file)
            } else {
                referenceList -= identifier
                file.update(identifier)
            }
        }

        for (identifier in referenceList) {
            val file = FilePlanet(identifier, loader)
            addFile(file)
        }

        oldIdentifierList = newIdentifierList
    }

    init {
        loader.onRemoteChange {
            GlobalScope.launch(Dispatchers.Default) {
                sync()
            }
        }

        GlobalScope.launch(Dispatchers.Default) {
            sync()
        }
    }
}
