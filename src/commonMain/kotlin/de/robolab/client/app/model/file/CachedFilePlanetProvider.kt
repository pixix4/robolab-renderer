package de.robolab.client.app.model.file

import de.robolab.client.app.model.file.provider.FilePlanet
import de.robolab.common.planet.Planet
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CachedFilePlanetProvider(
    private val fileNavigationManager: FileNavigationManager
) {

    private val map: MutableMap<String, Entry> = mutableMapOf()

    operator fun get(name: String): ObservableValue<Planet?> {
        val entry = map.getOrPut(name) { Entry(name) }
        entry.update()
        return entry.observable
    }

    suspend fun loadPlanet(name: String): Planet? {
        val entry = map.getOrPut(name) { Entry(name) }
        entry.update()
        return entry.get()
    }

    inner class Entry(private val name: String) {

        private var loading = false
        val observable = property<Planet>()
        private var filePlanet: FilePlanet? = null

        private val continuationListener = mutableListOf<() -> Unit>()

        fun update() {
            if (loading) return

            loading = true
            GlobalScope.launch(Dispatchers.Main) {
                if (filePlanet == null) {
                    filePlanet = fileNavigationManager.searchPlanet(name)

                    val entry = filePlanet
                    if (entry != null) {
                        if (observable.isBound) observable.unbind()
                        observable.bind(entry.planetFile.planetProperty)
                    }
                }

                filePlanet?.load()
                loading = false

                for (l in continuationListener) l()
                continuationListener.clear()
            }
        }

        suspend fun get(): Planet? {
            return if (loading) {
                suspendCoroutine { continuation ->
                    continuationListener += {
                        continuation.resume(observable.value)
                    }
                }
            } else observable.value
        }

        init {
            update()
        }
    }
}
