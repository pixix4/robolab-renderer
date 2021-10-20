package de.robolab.client.app.model.file.provider

import de.robolab.client.utils.cache.ICacheStorage
import de.robolab.common.planet.Planet
import de.robolab.common.planet.PlanetFile
import de.robolab.common.utils.autoLogger
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FilePlanet(
    var loader: IFilePlanetLoader,
    private var id: String,
    private val cacheEntry: ICacheStorage.Entry,
) {

    private val stateProperty = property<State>()
    private var state by stateProperty

    val planetFile = PlanetFile("")

    val isLoadedProperty = stateProperty.mapBinding { it != null }
    val hasLocalChangesProperty = stateProperty.mapBinding { it != null && it.hasLocalChanges }
    val hasRemoteChangesProperty = stateProperty.mapBinding { it != null && it.hasRemoteChanges }
    val hasConflictsProperty = stateProperty.mapBinding { it != null && it.hasConflicts }

    suspend fun save(): Boolean {
        val s = state ?: return false
        loader.savePlanet(id, s.editLines) ?: return false
        update()
        return true
    }

    suspend fun copy(parentId: String): Boolean {
        val s = state ?: return false

        val planetFileCopy = PlanetFile(s.editLines)
        planetFileCopy.setName(planetFileCopy.planet.name + " - Copy")
        loader.createPlanet(parentId, planetFileCopy.planet) ?: return false
        return true
    }

    suspend fun delete(): Boolean {
        loader.deletePlanet(id)
        state = null
        return true
    }

    fun resetLocalEdits() {
        val s = state ?: return
        state = s.copy(baseLines = s.remoteLines, editLines = s.remoteLines).simplify()
    }

    suspend fun update() {
        val (metadata, content) = loader.loadPlanet(id) ?: return
        val s = state

        val newState = s?.copy(
            metadata = metadata,
            remoteLines = content
        )?.simplify() ?: State(metadata, content)

        if (!newState.hasLocalChanges) {
            planetFile.planetProperty.clear()
        }

        if (newState != s) {
            state = newState
        }
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

    init {
        GlobalScope.launch {
            try {
                val cacheContent = cacheEntry.read() ?: throw NullPointerException()
                state = Json.decodeFromString<State>(cacheContent)
            } catch (e: Exception) {
                autoLogger.debug("Could not load cache-state",e)
            }

            update()
        }

        stateProperty.onChange {
            val editLines = state?.editLines ?: Planet.EMPTY
            if (editLines != planetFile.planet) {
                planetFile.planet = editLines
            }
            GlobalScope.launch {
                cacheEntry.write(Json.encodeToString(state))
            }
        }

        planetFile.planetProperty.onChange {
            val content = planetFile.planet
            val s = state
            if (s == null) {
                if (content != Planet.EMPTY) {
                    throw IllegalStateException()
                }
            } else {
                if (content != s.editLines) {
                    state = State(
                        s.metadata,
                        remoteLines = state?.remoteLines ?: Planet.EMPTY,
                        baseLines = state?.baseLines ?: Planet.EMPTY,
                        editLines = content,
                    )
                }
            }
        }
    }

    @Serializable
    data class State(
        val metadata: RemoteMetadata.Planet,
        val remoteLines: Planet,
        val baseLines: Planet,
        val editLines: Planet,
    ) {

        constructor(metadata: RemoteMetadata.Planet, lines: Planet) : this(metadata, lines, lines, lines)

        val hasLocalChanges: Boolean
            get() = editLines != baseLines

        val hasRemoteChanges: Boolean
            get() = remoteLines != baseLines

        val hasConflicts: Boolean
            get() = hasLocalChanges && hasRemoteChanges && editLines != remoteLines

        fun simplify(): State {
            if (hasRemoteChanges && !hasLocalChanges) {
                return copy(
                    editLines = remoteLines,
                    baseLines = remoteLines
                )
            } else if (hasRemoteChanges && hasLocalChanges && editLines == remoteLines) {
                return copy(baseLines = remoteLines)
            }

            return this
        }
    }
}
