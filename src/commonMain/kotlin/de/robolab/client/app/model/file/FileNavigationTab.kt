package de.robolab.client.app.model.file

import de.robolab.client.app.model.base.INavigationBarEntry
import de.robolab.client.app.model.base.INavigationBarList
import de.robolab.client.app.model.base.INavigationBarSearchList
import de.robolab.client.app.model.base.INavigationBarTab
import de.robolab.client.app.model.file.provider.IFilePlanetIdentifier
import de.robolab.client.app.model.file.provider.IFilePlanetLoader
import de.westermann.kobserve.property.join

class FileNavigationTab<T : IFilePlanetIdentifier>(
    private val manager: FileNavigationManager,
    private val loader: IFilePlanetLoader<T>,
) : INavigationBarTab(
    loader.nameProperty.join(loader.descProperty) { name, desc ->
        "$name: $desc"
    },
    loader.iconProperty
) {

    override fun createSearchList(parent: INavigationBarList): INavigationBarSearchList {
        return FileNavigationSearchList<T>(this, loader, activeProperty.value)
    }

    @Suppress("UNCHECKED_CAST")
    override fun openEntry(entry: INavigationBarEntry, asNewTab: Boolean) {
        if (entry is FileNavigationList<*>.Entry) {
            if (entry.entry.isDirectory) {
                activeProperty.value = FileNavigationList(this, loader, entry.entry as T, activeProperty.value)
            } else {
                manager.openFileEntry(loader, entry.entry as T, asNewTab)
            }
        } else if (entry is FileNavigationSearchList<*>.Entry) {
            if (entry.entry.isDirectory) {
                activeProperty.value = FileNavigationList(this, loader, entry.entry as T, activeProperty.value)
            } else {
                manager.openFileEntry(loader, entry.entry as T, asNewTab)
            }
        }
    }

    init {
        activeProperty.value = FileNavigationList<T>(this, loader, null, null)

        loader.onRemoteChange {
            @Suppress("UNCHECKED_CAST")
            (activeProperty.value as? RepositoryList<T>)?.onChange(it)
        }
    }

    interface LoaderEventListener<T : IFilePlanetIdentifier> {

        val loader: IFilePlanetLoader<T>
        fun onChange(entry: T?)
    }

    interface RepositoryList<T : IFilePlanetIdentifier> : INavigationBarList, LoaderEventListener<T>

}
