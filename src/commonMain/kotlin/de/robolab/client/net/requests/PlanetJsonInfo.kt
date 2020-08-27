package de.robolab.client.net.requests

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.format
import com.soywiz.klock.parseUtc
import de.robolab.client.app.model.base.SearchRequest
import de.robolab.client.app.model.file.provider.IFilePlanetIdentifier
import de.robolab.common.planet.ID
import de.robolab.common.planet.IDSerializer
import de.robolab.common.planet.IPlanetInfo
import de.robolab.common.planet.Planet
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class PlanetJsonInfo(
    @Serializable(with = IDSerializer::class)
    override val id: ID,
    override val name: String,
    @SerialName("lastModified")
    val lastModifiedString: String,
    override val tags: Map<String, List<String>> = emptyMap()

) : IFilePlanetIdentifier, IPlanetInfo<ID> {

    constructor(id: ID, name: String, lastModified: DateTime, tags: Map<String, List<String>> = emptyMap()) :
            this(id, name, DateFormat.FORMAT1.format(lastModified), tags)

    @Transient
    private var _dateTime: DateTime? = null

    override val lastModified: DateTime
        get() {
            val dateTime0 = _dateTime

            if (dateTime0 == null) {
                val dateTime1 = DateFormat.FORMAT1.parseUtc(lastModifiedString)
                _dateTime = dateTime1
                return dateTime1
            }

            return dateTime0
        }

    override val isDirectory = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlanetJsonInfo) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun withMTime(time: DateTime): PlanetJsonInfo {
        return PlanetJsonInfo(
            id = id,
            name = name,
            lastModifiedString = DateFormat.FORMAT1.format(time)
        )
    }

    override fun matchesSearch(request: SearchRequest, planet: Planet): Boolean = request.matches(this)
}