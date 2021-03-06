package de.robolab.client.app.model.file

import de.robolab.client.app.controller.FilePlanetController
import de.robolab.client.app.model.base.INavigationBarEntry
import de.robolab.client.app.model.base.INavigationBarTab
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.provider.IFilePlanetLoader
import de.robolab.client.app.model.file.provider.RemoteIdentifier
import de.robolab.client.app.model.file.provider.RemoteMetadata
import de.robolab.client.app.viewmodel.SideBarContentViewModel
import de.robolab.client.renderer.Exporter
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.utils.MenuBuilder
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.utils.runAsync
import de.robolab.common.planet.PlanetFile
import de.robolab.common.planet.Planet
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.formatDateTime
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sync
import de.westermann.kobserve.property.constObservable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class FileNavigationList(
    private val tab: FileNavigationTab,
    override val loader: IFilePlanetLoader,
    private val filePlanetController: FilePlanetController,
    private val id: String = "",
    metadata: RemoteMetadata.Directory? = null,
    override val parent: SideBarContentViewModel? = null
) : FileNavigationTab.RepositoryList {

    override val nameProperty = constObservable(metadata?.name ?: loader.nameProperty.value)

    override val childrenProperty = observableListOf<INavigationBarEntry>()

    override fun onChange(entry: RemoteIdentifier) {
        if (id == entry.id) {
            GlobalScope.launch {
                val planets = loader.listPlanets(id) ?: emptyList()

                runAsync {
                    childrenProperty.sync(planets.map {
                        mapEntry(tab, id, loader, it, filePlanetController)
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
            entry: RemoteIdentifier,
            filePlanetController: FilePlanetController,
        ) = when (entry.metadata) {
            is RemoteMetadata.Planet -> EntryPlanet(
                tab,
                parentId,
                loader,
                entry.id,
                entry.metadata,
                filePlanetController
            )
            is RemoteMetadata.Directory -> EntryDirectory(tab, loader, entry.id, entry.metadata)
        }
    }

    init {
        GlobalScope.launch {
            val planets = loader.listPlanets(id) ?: emptyList()

            runAsync {
                childrenProperty.sync(planets.map {
                    mapEntry(tab, id, loader, it, filePlanetController)
                })
            }
        }
    }

    class EntryPlanet(
        override val tab: INavigationBarTab,
        private val parentId: String?,
        val loader: IFilePlanetLoader,
        val id: String,
        val metadata: RemoteMetadata.Planet,
        private val filePlanetController: FilePlanetController,
    ) : INavigationBarEntry {

        override val nameProperty = constObservable(metadata.name)

        override val subtitleProperty = constObservable(formatDateTime(metadata.lastModified,"YYYY-MM-DD HH:mm"))

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
                        val filePlanet = filePlanetController.getFilePlanet(loader, id)
                        filePlanet.copy(parentId)
                    }
                }
                action("Delete") {
                    GlobalScope.launch(Dispatchers.Main) {
                        val filePlanet = filePlanetController.getFilePlanet(loader, id)
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
                planet = lines
                timestamp = Clock.System.now().toEpochMilliseconds()
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
                Exporter.renderToCanvas(
                    p,
                    canvas,
                    drawName = false,
                    drawNumbers = false,
                    theme = PreferenceStorage.selectedTheme.theme
                )
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
            if (metadata.childrenCount != null) {
                append(metadata.childrenCount)
                append(" entr")
                if (metadata.childrenCount != 1) {
                    append("ies")
                } else {
                    append('y')
                }
            }
        })

        override val enabledProperty = loader.availableProperty

        override val statusIconProperty = constObservable(listOf(MaterialIcon.FOLDER_OPEN))
    }
}
