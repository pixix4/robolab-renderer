package de.robolab.client.app.model.file.provider

import com.soywiz.klock.DateTime
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.net.http
import de.robolab.client.net.web
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.property
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.coroutines.CoroutineContext

class LocalWebPlanetLoader : IFilePlanetLoader<LocalWebPlanetLoader.FileIdentifier>, CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Main

    override val onRemoteChange = EventHandler<FileIdentifier?>()

    override val nameProperty = constObservable("Local web")

    override val descProperty = constObservable(window.location.hostname)

    override val iconProperty = constObservable(MaterialIcon.LANGUAGE)

    override val availableProperty = constObservable(true)

    override val planetCountProperty = property(0)

    override suspend fun loadPlanet(identifier: FileIdentifier): Pair<FileIdentifier, List<String>>? {
        val lines = http {
            web(identifier.url)
        }.exec().body?.split('\n') ?: emptyList()

        return identifier to lines
    }

    override suspend fun savePlanet(identifier: FileIdentifier, lines: List<String>): FileIdentifier? {
        println("Currently not supported!")
        return null
    }

    override suspend fun createPlanet(identifier: FileIdentifier?, lines: List<String>) {
        println("Currently not supported!")
    }

    override suspend fun deletePlanet(identifier: FileIdentifier) {
        println("Currently not supported!")
    }

    override suspend fun listPlanets(identifier: FileIdentifier?): List<FileIdentifier> {
        val names = http {
            web("/planets")
        }.exec().parse(ListSerializer(String.serializer())) ?: emptyList()

        planetCountProperty.value = names.size
        return names.map { name ->
            FileIdentifier("/planet/$name", name)
        }
    }

    override suspend fun searchPlanets(search: String): List<FileIdentifier> {
        val identifier = listPlanets(null)

        return identifier.filter {
            it.name.contains(search, true)
        }.sortedBy {
            it.name.length
        }
    }

    class FileIdentifier(
        val url: String,
        override val name: String,
        override val lastModified: DateTime = DateTime.Companion.fromUnix(0)
    ) : IFilePlanetIdentifier {

        override val isDirectory = false

        override val childrenCount = 0

        override val path = emptyList<String>()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is FileIdentifier) return false

            if (url != other.url) return false

            return true
        }

        override fun hashCode(): Int {
            return url.hashCode()
        }
    }

    companion object : IFilePlanetLoaderFactory {

        override val protocol = "local"

        override val usage: String = "$protocol://"

        override fun create(uri: String): IFilePlanetLoader<*>? {
            return LocalWebPlanetLoader()
        }
    }

    init {
        GlobalScope.launch {
            listPlanets()
        }
    }
}
