package de.robolab.client.app.model.file.provider

import de.robolab.common.parser.PlanetFile
import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class FilePlanet(
    private val loader: IFilePlanetLoader,
    private var id: String
) {

    private val localMetadataProperty = property<RemoteMetadata.Planet>()
    private var localMetadata by localMetadataProperty

    private val remoteMetadataProperty = property<RemoteMetadata.Planet>()
    private var remoteMetadata by remoteMetadataProperty

    val planetFile = PlanetFile(emptyList())

    val isLoadedProperty = localMetadataProperty.mapBinding { it != null }
    val hasLocalChangesProperty = planetFile.history.canUndoProperty
    val hasRemoteChangesProperty = localMetadataProperty.join(remoteMetadataProperty) { local, remote ->
        local?.lastModified != remote?.lastModified
    }

    suspend fun save(): Boolean {
        localMetadata ?: return false

        val identifier = loader.savePlanet(id, planetFile.content) ?: return false

        planetFile.history.clear()
        id = identifier.id
        localMetadata = identifier.metadata as? RemoteMetadata.Planet ?: throw IllegalStateException()
        remoteMetadata = identifier.metadata as? RemoteMetadata.Planet ?: throw IllegalStateException()
        return true
    }

    suspend fun copy(parentId: String): Boolean {
        localMetadata ?: return false

        val content = PlanetFile(planetFile.content)
        content.setName(content.planet.name + " - Copy")
        loader.createPlanet(parentId, content.content) ?: return false
        return true
    }

    suspend fun delete(): Boolean {
        return loader.deletePlanet(id)
    }

    suspend fun load(): Boolean {
        val (metadata, content) = loader.loadPlanet(id) ?: return false

        planetFile.resetContent(content)
        localMetadata = metadata
        remoteMetadata = metadata
        return true
    }

    suspend fun update(metadata: RemoteMetadata.Planet): Boolean {
        val local = localMetadata
        val remote = remoteMetadata

        remoteMetadata = metadata

        if (local == null || remote?.lastModified == metadata.lastModified) {
            return true
        }

        val (newMetadata, content) = loader.loadPlanet(id) ?: return false

        planetFile.resetContent(content)
        localMetadata = newMetadata
        remoteMetadata = newMetadata
        return true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FilePlanet) return false

        if (loader != other.loader) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = loader.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }
}
