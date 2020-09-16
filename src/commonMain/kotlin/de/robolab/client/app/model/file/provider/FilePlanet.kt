package de.robolab.client.app.model.file.provider

import de.robolab.client.app.model.base.SearchRequest
import de.robolab.common.parser.PlanetFile
import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class FilePlanet<T : IFilePlanetIdentifier>(
    private val loader: IFilePlanetLoader<T>,
    identifier: T
) {

    private val localIdentifierProperty = property<T>()
    var localIdentifier by localIdentifierProperty

    private val remoteIdentifierProperty = property<T?>(identifier)
    var remoteIdentifier by remoteIdentifierProperty

    val planetFile = PlanetFile(emptyList())

    val isLoadedProperty = localIdentifierProperty.mapBinding { it != null }
    val hasLocalChangesProperty = planetFile.history.canUndoProperty
    val hasRemoteChangesProperty = localIdentifierProperty.join(remoteIdentifierProperty) { local, remote ->
        local?.lastModified != remote?.lastModified
    }

    suspend fun save() {
        val local = localIdentifier ?: return
        val identifier = loader.savePlanet(local, planetFile.content) ?: return

        planetFile.history.clear()
        localIdentifier = identifier
        remoteIdentifier = identifier
    }

    suspend fun copy(base: T?) {
        val content = PlanetFile(planetFile.content)
        content.setName(content.planet.name + " - Copy")
        loader.createPlanet(base, content.content)
    }

    suspend fun delete() {
        loader.deletePlanet(localIdentifier ?: return)
    }

    suspend fun load() {
        val remote = remoteIdentifier ?: return
        val (identifier, content) = loader.loadPlanet(remote) ?: return

        planetFile.resetContent(content)
        localIdentifier = identifier
        remoteIdentifier = identifier
    }

    suspend fun update(identifier: T) {
        val local = localIdentifier
        val remote = remoteIdentifier

        remoteIdentifier = identifier

        if (local == null || remote?.lastModified == identifier.lastModified) {
            return
        }

        val (newIdentifier, content) = loader.loadPlanet(identifier) ?: return

        planetFile.resetContent(content)
        localIdentifier = newIdentifier
        remoteIdentifier = newIdentifier
    }

    fun matchesSearch(request: SearchRequest): Boolean =
        (localIdentifier ?: remoteIdentifier)?.matchesSearch(request, planetFile.planet)
            ?: request.matches(planetFile.planet)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FilePlanet<*>) return false

        if (loader != other.loader) return false
        if (remoteIdentifier != other.remoteIdentifier) return false

        return true
    }

    override fun hashCode(): Int {
        var result = loader.hashCode()
        result = 31 * result + remoteIdentifier.hashCode()
        return result
    }
}
