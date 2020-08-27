package de.robolab.client.app.model.file.provider

import de.robolab.client.app.model.base.INavigationBarEntry
import de.robolab.client.app.model.base.INavigationBarGroup
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.FilePlanetEntry
import de.westermann.kobserve.base.ObservableMutableList
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sortByObservable
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.mapBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FilePlanetProvider<T : IFilePlanetIdentifier>(
    private val loader: IFilePlanetLoader<T>,
    override val parent: FilePlanetProvider<T>? = null,
    private val identifier: T? = null
) : INavigationBarGroup {

    private val planetList: ObservableMutableList<INavigationBarEntry> = observableListOf()

    val sortedEntries = planetList
        .sortByObservable { it.titleProperty.value.toLowerCase() }

    override val entryList = constObservable(sortedEntries)

    override val titleProperty: ObservableValue<String> = if (identifier == null) {
        loader.nameProperty.join(loader.descProperty) { name, desc ->
            "$name $desc"
        }
    } else {
        constObservable(identifier.name)
    }

    override val subtitleProperty: ObservableValue<String> = if (identifier == null) {
        planetList.mapBinding { "${it.size} entries" }
    } else {
        constObservable("")
    }

    override val tabNameProperty: ObservableValue<String> = titleProperty
    override val hasContextMenu: Boolean = false
    override val statusIconProperty: ObservableValue<List<MaterialIcon>> = if (identifier == null) {
        loader.iconProperty.mapBinding { listOf(it) }
    } else {
        constObservable(listOf(MaterialIcon.FOLDER_OPEN))
    }

    private var oldIdentifierList = emptyList<T>()

    private suspend fun addFile(file: INavigationBarEntry) = withContext(Dispatchers.Main) {
        planetList += file
    }

    private suspend fun removeFile(file: INavigationBarEntry) = withContext(Dispatchers.Main) {
        planetList -= file
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun sync() {
        val newIdentifierList = loader.listPlanets(identifier)
        if (newIdentifierList == oldIdentifierList) {
            return
        }

        val currentList = planetList.toList()
        val referenceList = newIdentifierList.toMutableList()


        for (file in currentList) {
            val idList = if (file is FilePlanet<*>) {
                listOf(
                    file.localIdentifier as T?,
                    file.remoteIdentifier as T?,
                )
            } else {
                file as FilePlanetProvider<T>
                listOf(file.identifier)
            }

            val identifier = referenceList.find {
                it in idList
            }

            if (identifier == null) {
                removeFile(file)
            } else {
                referenceList -= identifier

                if (file is FilePlanet<*>) {
                    file as FilePlanet<T>
                    file.update(identifier)
                } else {
                    file as FilePlanetProvider<T>
                    // TODO
                }
            }
        }

        for (identifier in referenceList) {
            val file = if (identifier.isDirectory) {
                FilePlanetProvider(loader, this, identifier)
            } else {
                FilePlanetEntry(FilePlanet(identifier, loader))
            }
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
