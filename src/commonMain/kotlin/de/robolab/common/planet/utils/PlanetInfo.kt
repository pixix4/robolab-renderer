package de.robolab.common.planet.utils

import de.robolab.common.planet.Planet
import de.robolab.common.utils.DateFormat
import de.robolab.common.utils.formatDateTime
import de.robolab.common.utils.parseDateTime
import kotlinx.datetime.Instant

interface IPlanetInfo<IDType> {
    val id: IDType
    val name: String
    val lastModified: Instant
    val tags: Map<String, List<String>>

    fun withMTime(time: Instant): IPlanetInfo<IDType>
}


data class ServerPlanetInfo(
    override val id: String,
    override val name: String,
    val lastModifiedString: String,
    override val tags: Map<String, List<String>> = emptyMap()
) : IPlanetInfo<String> {

    override val lastModified: Instant by lazy {
        parseDateTime(lastModifiedString, DateFormat.FORMAT1)
    }

    constructor(id: String, name: String, lastModified: Instant, tags: Map<String, List<String>> = emptyMap()) :
            this(id, name, formatDateTime(lastModified, DateFormat.FORMAT1), tags)

    companion object {
        fun fromPlanet(id: String, planet: Planet?, lastModified: Instant, nameOverride: String?=null) =
            ServerPlanetInfo(
                id,
                nameOverride?:planet?.name ?: randomName(),
                lastModified,
                planet?.tags.orEmpty()
            )
        fun fromPlanet(id: String, planet: Planet, lastModified: Instant, nameOverride: String?=null) =
            ServerPlanetInfo(
                id,
                nameOverride?:planet.name,
                lastModified,
                planet.tags
            )
    }

    override fun withMTime(time: Instant): ServerPlanetInfo {
        return copy(lastModifiedString = formatDateTime(time, DateFormat.FORMAT1))
    }
}
