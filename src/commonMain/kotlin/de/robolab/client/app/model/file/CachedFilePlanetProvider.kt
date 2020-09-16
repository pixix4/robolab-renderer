package de.robolab.client.app.model.file

import de.robolab.client.app.model.file.provider.FilePlanet
import de.robolab.common.planet.Planet
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CachedFilePlanetProvider(
    private val fileNavigationRoot: FileNavigationRoot
) {

    private val map: MutableMap<String, Entry> = mutableMapOf()

    operator fun get(name: String): ObservableValue<Planet?> {
        val entry = map.getOrPut(name) { Entry(name)}
        entry.update()
        return entry.observable
    }

    inner class Entry(private val name: String) {

        private var loading = false
        val observable = property<Planet>()
        private var filePlanet :  FilePlanet<*>? = null

        fun update() {
            if (loading) return

            loading = true
            GlobalScope.launch(Dispatchers.Main) {
                if (filePlanet == null) {
                    filePlanet = fileNavigationRoot.searchPlanet(name)

                    val entry = filePlanet
                    if (entry != null) {
                        observable.bind(entry.planetFile.planetProperty)
                    }
                }

                filePlanet?.load()
                loading = false
            }
        }

        init {
            update()
        }
    }
}
