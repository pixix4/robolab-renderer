package de.robolab.client.app.model.file

import de.robolab.client.app.controller.NavigationBarController
import de.robolab.client.app.model.base.INavigationBarEntryRoot
import de.robolab.client.app.model.base.INavigationBarList
import de.robolab.client.app.model.file.provider.IFilePlanetIdentifier
import de.robolab.client.app.model.file.provider.IFilePlanetLoader
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.property

class FileEntryNavigationRootList<T : IFilePlanetIdentifier>(
    private val root: FileNavigationRoot,
    private val loader: IFilePlanetLoader<T>,
) : INavigationBarEntryRoot, NavigationBarController.Tab {

    override val label = loader.nameProperty.join(loader.descProperty) { name, desc ->
        "$name: $desc"
    }
    override val icon = loader.iconProperty

    override val searchProperty = property("")

    override fun submitSearch() {

    }

    private val activeListProperty = property<INavigationBarList>(FileEntryNavigationList(this, loader, null))
    val activeList by activeListProperty

    override val childrenProperty = searchProperty.join(activeListProperty) { search, active ->
        if (search.isEmpty()) {
            active.childrenProperty
        } else {
            val current = active as? FileEntryNavigationList<*>
            current?.createSearchList(search)?.childrenProperty ?: active.childrenProperty
        }
    }

    override val parentNameProperty = activeListProperty.flatMapBinding {
        it.parentNameProperty
    }

    override fun openParent() {
        activeList.openParent()
    }

    fun openEntryList(
        entry: T? = null,
        parents: List<T> = emptyList()
    ) {
        activeListProperty.value = FileEntryNavigationList(this, loader, entry, parents)
    }

    fun openFileEntry(entry: T, asNewTab: Boolean) {
        root.openFileEntry(loader, entry, asNewTab)
    }

    init {
        loader.onRemoteChange {
            @Suppress("UNCHECKED_CAST")
            (activeList as? RepositoryList<T>)?.onChange(it)
        }
    }

    interface LoaderEventListener<T : IFilePlanetIdentifier> {

        val loader: IFilePlanetLoader<T>
        fun onChange(entry: T?)
    }

    interface RepositoryList<T : IFilePlanetIdentifier> : INavigationBarList, LoaderEventListener<T>

}
