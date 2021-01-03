package de.robolab.client.app.model.file

import de.robolab.client.app.model.base.*
import de.robolab.client.app.model.file.provider.IFilePlanetIdentifier
import de.robolab.client.app.model.file.provider.IFilePlanetLoader
import de.robolab.client.utils.MenuBuilder
import de.robolab.client.utils.runAsync
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.property
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FileNavigationSearchList<T : IFilePlanetIdentifier>(
    private val tab: FileNavigationTab<T>,
    val loader: IFilePlanetLoader<T>,
    override val parent: INavigationBarList?
) : INavigationBarSearchList {

    override val nameProperty = constObservable("Search")

    override val searchProperty = property("")

    override val childrenProperty = observableListOf<Entry>()

    init {
        searchProperty.onChange {
            GlobalScope.launch {
                val planets = loader.searchPlanets(searchProperty.value)

                runAsync {
                    childrenProperty.addAll(planets.map { Entry(it) })
                }
            }
        }
    }

    inner class Entry(
        val entry: T
    ) : INavigationBarEntry {

        override val nameProperty = constObservable(entry.name)

        override val tab = this@FileNavigationSearchList.tab

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

        override fun MenuBuilder.contextMenu() {
            if (entry.isDirectory) {
                return
            }

            name = "Planet ${entry.name}"
            action("Open in new tab") {
                open(true)
            }
        }
    }
}
