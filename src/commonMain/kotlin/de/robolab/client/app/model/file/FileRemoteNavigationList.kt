package de.robolab.client.app.model.file

import de.robolab.client.app.model.base.INavigationBarEntry
import de.robolab.client.app.model.base.INavigationBarList
import de.robolab.client.app.model.file.provider.IFilePlanetIdentifier
import de.robolab.client.app.model.file.provider.IFilePlanetLoader
import de.westermann.kobserve.event.EventListener
import de.westermann.kobserve.event.now
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sync
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.mapBinding

class FileRemoteNavigationList(
    private val root: FileNavigationRoot
) : INavigationBarList {

    override val parentNameProperty = constObservable<String?>(null)

    override fun openParent() {
        throw IllegalStateException()
    }

    override val childrenProperty = observableListOf<Entry>()

    private fun<T: IFilePlanetIdentifier> onRemoteChange(loader: IFilePlanetLoader<*>, entry: T?) {
        val remoteList = root.activeList as? RepositoryList<*> ?: return

        if (remoteList.loader == loader) {
            @Suppress("UNCHECKED_CAST") val list = remoteList as RepositoryList<T>
            list.onChange(entry)
        }
    }

    init {
        val listeners = mutableListOf<EventListener<*>>()
        root.fileLoaderProperty.onChange.now {
            childrenProperty.sync(root.fileLoaderProperty.value.map {
                Entry(it)
            })

            for (l in listeners) l.detach()
            listeners.clear()
            for (fileLoader in root.fileLoaderProperty.value) {
                listeners += fileLoader.onRemoteChange.reference {
                    onRemoteChange(fileLoader, it)
                }
            }
        }
    }


    interface LoaderEventListener<T : IFilePlanetIdentifier> {

        val loader: IFilePlanetLoader<T>
        fun onChange(entry: T?)
    }

    interface RepositoryList<T : IFilePlanetIdentifier> : INavigationBarList, LoaderEventListener<T>

    inner class Entry(
        val loader: IFilePlanetLoader<*>
    ) : INavigationBarEntry {

        override val nameProperty = loader.nameProperty

        override val subtitleProperty = loader.planetCountProperty.mapBinding {
            buildString {
                append(it)
                append(" planet")
                if (it != 1) {
                    append("s")
                }
            }
        }

        override val enabledProperty = loader.availableProperty

        override val statusIconProperty = loader.iconProperty.mapBinding {
            listOf(it)
        }

        override fun open(asNewTab: Boolean) {
            root.openRemoteEntryList(loader)
        }
    }
}
