package de.robolab.client.app.model.file.provider

import com.soywiz.klock.DateTime
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.common.net.externalserializers.DateSerializer
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventHandler
import kotlinx.serialization.Serializable

interface IFilePlanetLoader {

    val id: String

    val onRemoteChange: EventHandler<RemoteIdentifier>

    suspend fun loadPlanet(id: String): Pair<RemoteMetadata.Planet, List<String>>?

    suspend fun savePlanet(id: String, lines: List<String>): RemoteIdentifier?

    suspend fun createPlanet(parentId: String, lines: List<String>): RemoteIdentifier?

    suspend fun deletePlanet(id: String): Boolean

    suspend fun listPlanets(id: String): List<RemoteIdentifier>?

    suspend fun searchPlanets(search: String, matchExact: Boolean = false): List<RemoteIdentifier>?

    val nameProperty: ObservableValue<String>
    val descProperty: ObservableValue<String>

    val planetCountProperty: ObservableValue<Int>

    val iconProperty: ObservableValue<MaterialIcon>

    val availableProperty: ObservableValue<Boolean>

    val supportedRemoteModes: List<RemoteMode>
    val remoteModeProperty: ObservableProperty<RemoteMode>
}

enum class RemoteMode {
    NESTED, FLAT, LIVE
}

data class RemoteIdentifier(
    val id: String,
    val metadata: RemoteMetadata
)

sealed class RemoteMetadata {

    abstract val name: String
    abstract val lastModified: DateTime

    @Serializable
    data class Planet(
        override val name: String,
        @Serializable(with = DateSerializer::class)
        override val lastModified: DateTime,
        val tags: Map<String, List<String>> = emptyMap(),
        val pointCount: Int? = null
    ) : RemoteMetadata()

    data class Directory(
        override val name: String,
        override val lastModified: DateTime,
        val childrenCount: Int? = null
    ) : RemoteMetadata()
}

interface IFilePlanetLoaderFactory {
    fun create(uri: String): IFilePlanetLoader?
}
