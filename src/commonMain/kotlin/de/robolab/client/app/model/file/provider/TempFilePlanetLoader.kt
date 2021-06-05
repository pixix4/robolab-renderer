package de.robolab.client.app.model.file.provider

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.common.planet.Planet
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.property

object TempFilePlanetLoader : IFilePlanetLoader {

    override val id = "temp-file-loader"

    private val map: MutableMap<String, Identifier> = mutableMapOf()

    data class Identifier(
        val id: String,
        var content: Planet,
        var metadata: RemoteMetadata.Planet
    )

    fun create(id: String, metadata: RemoteMetadata.Planet, content: Planet) {
        if (id !in map) {
            map += id to Identifier(
                id,
                content,
                metadata
            )
        }
    }

    override val onRemoteChange: EventHandler<RemoteIdentifier> = EventHandler()

    override suspend fun loadPlanet(id: String): Pair<RemoteMetadata.Planet, Planet>? {
        val identifier = map[id] ?: return null
        val tmp = loadTempFile(id)
        if (tmp != null) {
            identifier.metadata = tmp.first
            identifier.content = tmp.second
        }
        return identifier.metadata to identifier.content
    }

    override suspend fun savePlanet(id: String, planet: Planet): RemoteIdentifier? {
        val identifier = map[id] ?: return null
        val tmp = saveTempFile(identifier.id, planet) ?: return null

        identifier.metadata = tmp

        return RemoteIdentifier(id, identifier.metadata)
    }

    override suspend fun createPlanet(parentId: String, planet: Planet): RemoteIdentifier? {
        return null
    }

    override suspend fun deletePlanet(id: String): Boolean {
        return false
    }

    override suspend fun listPlanets(id: String): List<RemoteIdentifier> {
        return map.values.map {
            RemoteIdentifier(
                it.id,
                it.metadata
            )
        }.sortedBy { it.id }
    }

    override suspend fun searchPlanets(search: String, matchExact: Boolean): List<RemoteIdentifier>? {
        return null
    }

    override val nameProperty: ObservableValue<String> = constObservable("Temp")
    override val descProperty: ObservableValue<String> = constObservable("Temp")
    override val planetCountProperty: ObservableValue<Int> = constObservable(1)
    override val iconProperty: ObservableValue<MaterialIcon> = constObservable(MaterialIcon.HOURGLASS_EMPTY)
    override val availableProperty: ObservableValue<Boolean> = constObservable(true)

    override val supportedRemoteModes: List<RemoteMode> = listOf(RemoteMode.FLAT)
    override val remoteModeProperty: ObservableProperty<RemoteMode> = property(RemoteMode.FLAT)
}

expect fun loadTempFile(id: String): Pair<RemoteMetadata.Planet, Planet>?
expect fun saveTempFile(id: String, content: Planet): RemoteMetadata.Planet?
