package de.robolab.client.app.model.file.provider

import de.robolab.common.parser.PlanetFile
import de.westermann.kobserve.property.join
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property

class FilePlanet<T : IFilePlanetIdentifier>(
    identifier: T,
    private val loader: IFilePlanetLoader<T>
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
        val identifier = loader.saveContent(local, planetFile.content) ?: return

        planetFile.history.clear()
        localIdentifier = identifier
        remoteIdentifier = identifier
    }

    suspend fun load() {
        val remote = remoteIdentifier ?: return
        val (identifier, content) = loader.loadContent(remote) ?: return

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

        val (newIdentifier, content) = loader.loadContent(identifier) ?: return

        planetFile.resetContent(content)
        localIdentifier = newIdentifier
        remoteIdentifier = newIdentifier
    }
}
