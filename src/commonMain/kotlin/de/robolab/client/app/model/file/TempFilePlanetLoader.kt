package de.robolab.client.app.model.file

import com.soywiz.klock.DateTime
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.provider.FilePlanet
import de.robolab.client.app.model.file.provider.IFilePlanetIdentifier
import de.robolab.client.app.model.file.provider.IFilePlanetLoader
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.constObservable

object TempFilePlanetLoader : IFilePlanetLoader<TempFilePlanetLoader.Identifier> {

    class Identifier(override val name: String, var content: List<String>) : IFilePlanetIdentifier {
        override val isDirectory = false
        override val childrenCount = 0
        override val lastModified = DateTime(0)
        override val path: List<String> = emptyList()

    }

    fun create(name: String, content: List<String>): FilePlanet<Identifier> {
        return FilePlanet(TempFilePlanetLoader, Identifier(name, content))
    }

    override val onRemoteChange: EventHandler<Identifier?> = EventHandler()

    override suspend fun loadPlanet(identifier: Identifier): Pair<Identifier, List<String>> {
        val content = loadTempFile(identifier.name) ?: identifier.content
        return identifier to content
    }

    override suspend fun savePlanet(identifier: Identifier, lines: List<String>): Identifier? {
        saveTempFile(identifier.name, lines)
        return identifier
    }

    override suspend fun createPlanet(identifier: Identifier?, lines: List<String>) {
        throw NotImplementedError()
    }

    override suspend fun deletePlanet(identifier: Identifier) {
        throw NotImplementedError()
    }

    override suspend fun listPlanets(identifier: Identifier?): List<Identifier> {
        return emptyList()
    }

    override suspend fun searchPlanets(search: String, matchExact: Boolean): List<Identifier> {
        return emptyList()
    }

    override val nameProperty: ObservableValue<String> = constObservable("Temp")
    override val descProperty: ObservableValue<String> = constObservable("Temp")
    override val planetCountProperty: ObservableValue<Int> = constObservable(1)
    override val iconProperty: ObservableValue<MaterialIcon> = constObservable(MaterialIcon.HOURGLASS_EMPTY)
    override val availableProperty: ObservableValue<Boolean> = constObservable(true)
}

expect fun loadTempFile(file: String): List<String>?
expect fun saveTempFile(file: String, content: List<String>)
