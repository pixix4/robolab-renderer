package de.robolab.client.app.model.file

import com.soywiz.klock.DateTime
import de.robolab.client.app.model.base.INavigationBarEntry
import de.robolab.client.app.model.base.INavigationBarList
import de.robolab.client.app.model.base.INavigationBarTab
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.provider.FilePlanet
import de.robolab.client.app.model.file.provider.IFilePlanetLoader
import de.robolab.client.app.model.file.provider.RemoteIdentifier
import de.robolab.client.app.model.file.provider.RemoteMetadata
import de.robolab.client.renderer.Exporter
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.utils.MenuBuilder
import de.robolab.client.utils.runAsync
import de.robolab.common.parser.PlanetFile
import de.robolab.common.planet.Planet
import de.robolab.common.utils.Dimension
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sync
import de.westermann.kobserve.property.constObservable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FileNavigationList(
    private val tab: FileNavigationTab,
    override val loader: IFilePlanetLoader,
    private val id: String = "",
    metadata: RemoteMetadata.Directory? = null,
    override val parent: INavigationBarList? = null
) : FileNavigationTab.RepositoryList {

    override val nameProperty = constObservable(metadata?.name ?: loader.nameProperty.value)

    override val childrenProperty = observableListOf<INavigationBarEntry>()

    override fun onChange(entry: RemoteIdentifier) {
        if (id == entry.id) {
            GlobalScope.launch {
                val planets = loader.listPlanets(id) ?: emptyList()

                runAsync {
                    childrenProperty.sync(planets.map {
                        mapEntry(tab, id, loader, it)
                    })
                }
            }
        }
    }

    companion object {
        fun mapEntry(
            tab: INavigationBarTab,
            parentId: String?,
            loader: IFilePlanetLoader,
            entry: RemoteIdentifier
        ) = when (entry.metadata) {
            is RemoteMetadata.Planet -> EntryPlanet(tab, parentId, loader, entry.id, entry.metadata)
            is RemoteMetadata.Directory -> EntryDirectory(tab, loader, entry.id, entry.metadata)
        }
    }

    init {
        GlobalScope.launch {
            val planets = loader.listPlanets(id) ?: emptyList()

            runAsync {
                childrenProperty.sync(planets.map {
                    mapEntry(tab, id, loader, it)
                })
            }
        }
    }

    class EntryPlanet(
        override val tab: INavigationBarTab,
        private val parentId: String?,
        val loader: IFilePlanetLoader,
        val id: String,
        val metadata: RemoteMetadata.Planet
    ) : INavigationBarEntry {

        override val nameProperty = constObservable(metadata.name)

        override val subtitleProperty = constObservable(metadata.lastModified.local.format("yyyy-MM-dd HH:mm"))

        override val enabledProperty = loader.availableProperty

        override val statusIconProperty = constObservable(emptyList<MaterialIcon>())

        override fun MenuBuilder.contextMenu() {
            name = "Planet ${metadata.name}"
            action("Open in new tab") {
                open(true)
            }
            if (parentId != null) {
                action("Copy") {
                    GlobalScope.launch(Dispatchers.Main) {
                        val filePlanet = FilePlanet(loader, id)
                        filePlanet.load()
                        filePlanet.copy(parentId)
                    }
                }
                action("Delete") {
                    GlobalScope.launch(Dispatchers.Main) {
                        val filePlanet = FilePlanet(loader, id)
                        filePlanet.load()
                        filePlanet.delete()
                    }
                }
            }
        }

        private var planet: Planet? = null
        private var timestamp = -1L
        private suspend fun getPlanet(): Planet? {
            if (planet == null) {
                val (_, lines) = loader.loadPlanet(id) ?: return null
                planet = PlanetFile(lines).planet
                timestamp = DateTime.nowUnixLong()
            }
            return planet
        }

        override suspend fun getRenderDataTimestamp(): Long {
            getPlanet()
            return timestamp
        }

        override suspend fun <T : ICanvas> renderPreview(canvasCreator: (dimension: Dimension) -> T?): T? {
            val p = getPlanet() ?: return null
            val dimension = Exporter.getDimension(p)

            val canvas = canvasCreator(dimension)
            if (canvas != null) {
                Exporter.renderToCanvas(p, canvas, drawName = false, drawNumbers = false)
            }
            return canvas
        }
    }

    class EntryDirectory(
        override val tab: INavigationBarTab,
        val loader: IFilePlanetLoader,
        val id: String,
        val metadata: RemoteMetadata.Directory
    ) : INavigationBarEntry {

        override val nameProperty = constObservable(metadata.name)

        override val subtitleProperty = constObservable(buildString {
            append(metadata.childrenCount)
            append(" entr")
            if (metadata.childrenCount != 1) {
                append("ies")
            } else {
                append('y')
            }
        })

        override val enabledProperty = loader.availableProperty

        override val statusIconProperty = constObservable(listOf(MaterialIcon.FOLDER_OPEN))
    }
}
