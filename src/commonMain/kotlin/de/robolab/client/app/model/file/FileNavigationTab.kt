package de.robolab.client.app.model.file

import de.robolab.client.app.model.base.INavigationBarEntry
import de.robolab.client.app.model.base.INavigationBarList
import de.robolab.client.app.model.base.INavigationBarSearchList
import de.robolab.client.app.model.base.INavigationBarTab
import de.robolab.client.app.model.file.provider.IFilePlanetLoader
import de.robolab.client.app.model.file.provider.RemoteIdentifier
import de.westermann.kobserve.property.join

class FileNavigationTab(
    private val manager: FileNavigationManager,
    private val loader: IFilePlanetLoader,
) : INavigationBarTab(
    loader.nameProperty.join(loader.descProperty) { name, desc ->
        "$name: $desc"
    },
    loader.iconProperty
) {

    override fun createSearchList(parent: INavigationBarList): INavigationBarSearchList {
        return FileNavigationSearchList(this, loader, activeProperty.value)
    }

    @Suppress("UNCHECKED_CAST")
    override fun openEntry(entry: INavigationBarEntry, asNewTab: Boolean) {
        when (entry) {
            is FileNavigationList.EntryPlanet -> {
                manager.openFileEntry(loader, entry.id, asNewTab)
            }
            is FileNavigationList.EntryDirectory -> {
                activeProperty.value = FileNavigationList(this, loader, entry.id, entry.metadata, activeProperty.value)
            }
        }
    }

    init {
        activeProperty.value = FileNavigationList(this, loader)

        loader.onRemoteChange {
            @Suppress("UNCHECKED_CAST")
            (activeProperty.value as? RepositoryList)?.onChange(it)
        }
    }

    interface LoaderEventListener {

        val loader: IFilePlanetLoader
        fun onChange(entry: RemoteIdentifier)
    }

    interface RepositoryList : INavigationBarList, LoaderEventListener

}
