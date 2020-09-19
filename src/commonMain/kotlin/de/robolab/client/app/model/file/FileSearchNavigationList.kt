package de.robolab.client.app.model.file

import de.robolab.client.app.model.base.INavigationBarEntry
import de.robolab.client.app.model.base.INavigationBarList
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.provider.IFilePlanetIdentifier
import de.robolab.client.app.model.file.provider.IFilePlanetLoader
import de.robolab.client.utils.ContextMenu
import de.robolab.client.utils.buildContextMenu
import de.robolab.client.utils.runAsync
import de.robolab.common.utils.Point
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.constObservable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FileSearchNavigationList<T : IFilePlanetIdentifier>(
    private val root: FileNavigationRoot,
    val loader: IFilePlanetLoader<T>,
    private val search: String,
    private val parents: List<T> = emptyList()
) : INavigationBarList {

    override val parentNameProperty = constObservable("Search")

    override fun openParent() {
        root.openRemoteEntryList(
            loader,
            parents.lastOrNull(),
            parents,
        )
    }

    override val childrenProperty = observableListOf<Entry>()

    init {
        GlobalScope.launch {
            val planets = loader.searchPlanets(search)

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
            entry.path.joinToString("/").let {
                if (it.isEmpty()) {
                    entry.lastModified.format("yyyy-MM-dd HH:mm:ss z")
                } else it
            }
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
                root.openRemoteEntryList(loader, entry, parents + entry)
            } else {
                root.openFileEntry(loader, entry, asNewTab)
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
            }
        }
    }
}
