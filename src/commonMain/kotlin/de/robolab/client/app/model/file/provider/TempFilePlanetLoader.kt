package de.robolab.client.app.model.file.provider

import de.robolab.client.app.model.base.MaterialIcon
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.constObservable

object TempFilePlanetLoader : IFilePlanetLoader {

    private val map: MutableMap<String, Identifier> = mutableMapOf()

    data class Identifier(
        val id: String,
        var content: List<String>,
        var metadata: RemoteMetadata.Planet
    )

    fun create(id: String, metadata: RemoteMetadata.Planet, content: List<String>) {
        if (id !in map) {
            map += id to Identifier(
                id,
                content,
                metadata
            )
        }
    }

    override val onRemoteChange: EventHandler<RemoteIdentifier> = EventHandler()

    override suspend fun loadPlanet(id: String): Pair<RemoteMetadata.Planet, List<String>>? {
        val identifier = map[id] ?: return null
        val tmp = loadTempFile(id)
        if (tmp != null) {
            identifier.metadata = tmp.first
            identifier.content = tmp.second
        }
        return identifier.metadata to identifier.content
    }

    override suspend fun savePlanet(id: String, lines: List<String>): RemoteIdentifier? {
        val identifier = map[id] ?: return null
        val tmp = saveTempFile(identifier.id, lines) ?: return null

        identifier.metadata = tmp

        return RemoteIdentifier(id, identifier.metadata)
    }

    override suspend fun createPlanet(parentId: String, lines: List<String>): RemoteIdentifier? {
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
}

expect fun loadTempFile(id: String): Pair<RemoteMetadata.Planet, List<String>>?
expect fun saveTempFile(id: String, content: List<String>): RemoteMetadata.Planet?
