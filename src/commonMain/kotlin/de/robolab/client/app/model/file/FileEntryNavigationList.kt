package de.robolab.client.app.model.file

import de.robolab.client.app.model.base.INavigationBarEntry
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.provider.FilePlanet
import de.robolab.client.app.model.file.provider.IFilePlanetIdentifier
import de.robolab.client.app.model.file.provider.IFilePlanetLoader
import de.robolab.client.utils.ContextMenu
import de.robolab.client.utils.buildContextMenu
import de.robolab.client.utils.runAsync
import de.robolab.common.utils.Point
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sync
import de.westermann.kobserve.property.constObservable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FileEntryNavigationList<T : IFilePlanetIdentifier>(
    private val root: FileEntryNavigationRootList<T>,
    override val loader: IFilePlanetLoader<T>,
    private val entry: T?,
    private val parents: List<T> = emptyList()
) : FileEntryNavigationRootList.RepositoryList<T> {

    override val parentNameProperty = if (entry == null) {
        constObservable(null)
    } else {
        constObservable(entry.name)
    }

    override fun openParent() {
        if (entry != null) {
            root.openEntryList(
                parents.dropLast(1).lastOrNull(),
                parents.dropLast(1),
            )
        }
    }

    override val childrenProperty = observableListOf<Entry>()

    override fun onChange(entry: T?) {
        if (entry == this.entry) {
            GlobalScope.launch {
                val planets = loader.listPlanets(entry)

                runAsync {
                    childrenProperty.sync(planets.map { Entry(it) })
                }
            }
        }
    }

    fun createSearchList(search: String) = FileSearchNavigationList(
        root,
        loader,
        search,
        parents
    )

    init {
        GlobalScope.launch {
            val planets = loader.listPlanets(entry)

            runAsync {
                childrenProperty.addAll(planets.map { Entry(it) })
            }
        }
    }

    inner class Entry(
        val entry: T
    ) : INavigationBarEntry {

        override val nameProperty = constObservable(entry.name)

        override val subtitleProperty = constObservable(if (entry.isDirectory) {
            buildString {
                append(entry.childrenCount)
                append(" entr")
                if (entry.childrenCount != 1) {
                    append("ies")
                } else {
                    append('y')
                }
            }
        } else {
            entry.lastModified.format("yyyy-MM-dd HH:mm:ss z")
        })

        override val enabledProperty = loader.availableProperty

        override val statusIconProperty = constObservable(
            if (entry.isDirectory) {
                listOf(MaterialIcon.FOLDER_OPEN)
            } else {
                emptyList()
            }
        )

        override fun open(asNewTab: Boolean) {
            if (entry.isDirectory) {
                root.openEntryList(entry, parents + entry)
            } else {
                root.openFileEntry(entry, asNewTab)
            }
        }

        override fun contextMenu(position: Point): ContextMenu? {
            if (entry.isDirectory) {
                return null
            }

            return buildContextMenu(position, "Planet ${entry.name}") {
                action("Open in new tab") {
                    open(true)
                }
                action("Copy") {
                    GlobalScope.launch(Dispatchers.Main) {
                        val filePlanet = FilePlanet(loader, entry)
                        filePlanet.load()
                        filePlanet.copy(this@FileEntryNavigationList.entry)
                    }
                }
                action("Delete") {
                    GlobalScope.launch(Dispatchers.Main) {
                        val filePlanet = FilePlanet(loader, entry)
                        filePlanet.load()
                        filePlanet.delete()
                    }
                }
            }
        }
    }
}
