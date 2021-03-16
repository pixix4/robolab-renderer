package de.robolab.client.app.controller

import de.robolab.client.app.model.file.provider.FilePlanet
import de.robolab.client.app.model.file.provider.IFilePlanetLoader
import de.robolab.client.utils.cache.ICacheStorage
import de.robolab.client.utils.runAfterTimeoutInterval
import de.robolab.common.planet.Planet
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.property
import de.westermann.kobserve.property.readOnly
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FilePlanetController(
    val remoteServerController: RemoteServerController,
    val localServerController: LocalServerController,
    private val cacheStorage: ICacheStorage
) {

    private val idMap = mutableMapOf<String, FileFileHelper>()
    private val nameMap = mutableMapOf<String, PlanetPropertyHelper>()

    fun getFilePlanet(loader: IFilePlanetLoader, id: String): FilePlanet {
        val helper = idMap.getOrPut(getCacheId(loader, id)) {
            FileFileHelper(loader, id)
        }

        return helper.updateAndGet()
    }

    fun getPlanetObservable(name: String): ObservableValue<Planet> {
        val helper = nameMap.getOrPut(name) {
            PlanetPropertyHelper(name)
        }

        helper.update()

        return helper.observable
    }

    suspend fun getPlanet(name: String): Planet {
        val helper = nameMap.getOrPut(name) {
            PlanetPropertyHelper(name)
        }

        return helper.updateAndGet()
    }

    private fun updateIdMap(loader: IFilePlanetLoader) {
        for (helper in idMap.values) {
            helper.updateLoader(loader)
        }
    }

    private fun updateIdMap() {
        for (helper in idMap.values) {
            helper.update()
        }
    }

    private fun updateNameMap() {
        for (helper in nameMap.values) {
            helper.update()
        }
    }

    init {
        localServerController.localPlanetLoaderProperty.onChange {
            val loader = localServerController.localPlanetLoaderProperty.value
            if (loader != null) {
                updateIdMap(loader)
                updateNameMap()
            }
        }
        remoteServerController.remotePlanetLoaderProperty.onChange {
            val loader = remoteServerController.remotePlanetLoaderProperty.value
            if (loader != null) {
                updateIdMap(loader)
                updateNameMap()
            }
        }

        runAfterTimeoutInterval(60 * 1000) {
            updateIdMap()
        }
    }

    companion object {
        private fun getCacheId(loader: IFilePlanetLoader, id: String): String {
            return "file-${loader.id}-$id"
        }
    }

    private inner class PlanetPropertyHelper(
        private val name: String
    ) {

        val property = property(Planet.EMPTY)
        val observable = property.readOnly()

        private var filePlanet: FilePlanet? = null
            set(value) {
                if (value != field) {
                    field = value
                    if (property.isBound) {
                        property.unbind()
                    }
                    if (value != null) {
                        property.bind(value.planetFile.planetProperty)
                    }
                }
            }

        fun update() {
            for (helper in idMap.values) {
                if (helper.getName() == name) {
                    filePlanet = helper.updateAndGet()
                    return
                }
            }

            GlobalScope.launch {
                val loaderList = listOfNotNull(
                    remoteServerController.remotePlanetLoaderProperty.value,
                    localServerController.localPlanetLoaderProperty.value,
                )

                for (loader in loaderList) {
                    val planet = loader.searchPlanets(name, matchExact = true)?.firstOrNull() ?: continue
                    filePlanet = getFilePlanet(loader, planet.id)
                }
            }
        }

        suspend fun updateAndGet(): Planet {
            for (helper in idMap.values) {
                if (helper.getName() == name) {
                    filePlanet = helper.updateAndGet()
                    filePlanet?.update()
                    return observable.value
                }
            }

            val loaderList = listOfNotNull(
                remoteServerController.remotePlanetLoaderProperty.value,
                localServerController.localPlanetLoaderProperty.value,
            )

            for (loader in loaderList) {
                val planet = loader.searchPlanets(name, matchExact = true)?.firstOrNull() ?: continue
                filePlanet = getFilePlanet(loader, planet.id)
                filePlanet?.update()
                return observable.value
            }

            return observable.value
        }
    }

    private inner class FileFileHelper(
        private var loader: IFilePlanetLoader,
        private var id: String
    ) {

        private var filePlanet: FilePlanet? = null

        private fun getFilePlanet(): FilePlanet {
            val currentFilePlanet = filePlanet
            if (currentFilePlanet != null) return currentFilePlanet

            val filePlanetId = getCacheId(loader, id)
            val cacheEntry = cacheStorage.getEntry(filePlanetId)
            val newFilePlanet = FilePlanet(loader, id, cacheEntry)
            filePlanet = newFilePlanet
            return newFilePlanet
        }

        fun getName(): String? {
            return filePlanet?.planetFile?.planet?.name
        }

        fun updateAndGet(): FilePlanet {
            val file = getFilePlanet()
            update()
            return file
        }

        fun updateLoader(newLoader: IFilePlanetLoader) {
            if (loader.id != newLoader.id) return

            loader = newLoader
            filePlanet?.loader = newLoader

            update()
        }

        fun update() {
            GlobalScope.launch {
                filePlanet?.update()
            }
        }
    }
}
