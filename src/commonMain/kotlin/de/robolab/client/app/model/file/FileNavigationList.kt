package de.robolab.client.app.model.file

import de.robolab.client.app.model.base.INavigationBarEntry
import de.robolab.client.app.model.base.INavigationBarList
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.provider.FilePlanet
import de.robolab.client.app.model.file.provider.IFilePlanetIdentifier
import de.robolab.client.app.model.file.provider.IFilePlanetLoader
import de.robolab.client.utils.MenuBuilder
import de.robolab.client.utils.runAsync
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sync
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.mapBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FileNavigationList<T : IFilePlanetIdentifier>(
    private val tab: FileNavigationTab<T>,
    override val loader: IFilePlanetLoader<T>,
    private val entry: T?,
    override val parent: INavigationBarList?
) : FileNavigationTab.RepositoryList<T> {

    override val nameProperty = if (entry == null) {
        loader.nameProperty.mapBinding { it.toLowerCase() + ":/" }
    } else {
        constObservable(entry.name)
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

        override val tab = this@FileNavigationList.tab

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

        override fun MenuBuilder.contextMenu() {
            if (entry.isDirectory) {
                return
            }

            name = "Planet ${entry.name}"
            action("Open in new tab") {
                open(true)
            }
            action("Copy") {
                GlobalScope.launch(Dispatchers.Main) {
                    val filePlanet = FilePlanet(loader, entry)
                    filePlanet.load()
                    filePlanet.copy(this@FileNavigationList.entry)
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
