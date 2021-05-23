package de.robolab.client.net.requests

import kotlinx.datetime.Instant
import de.robolab.common.planet.ID
import de.robolab.common.planet.IDSerializer
import de.robolab.common.planet.IPlanetInfo
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

) : IPlanetInfo<ID> {

    constructor(id: ID, name: String, lastModified: Instant, tags: Map<String, List<String>> = emptyMap()) :
            this(id, name, lastModified.toString(), tags)

    @Transient
    private var _dateTime: Instant? = null

    override val lastModified: Instant
        get() {
            val dateTime0 = _dateTime

            if (dateTime0 == null) {
                val dateTime1 = Instant.parse(lastModifiedString)
                _dateTime = dateTime1
                return dateTime1
            }

            return dateTime0
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlanetJsonInfo) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun withMTime(time: Instant): PlanetJsonInfo {
        return PlanetJsonInfo(
            id = id,
            name = name,
            lastModifiedString = time.toString()
        )
    }
}
