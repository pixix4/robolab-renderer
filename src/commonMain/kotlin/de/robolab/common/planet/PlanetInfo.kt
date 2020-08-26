package de.robolab.common.planet

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.format
import com.soywiz.klock.parseUtc

interface IPlanetInfo<IDType> {
    val id: IDType
    val name: String
    val lastModified: DateTime
    val tags: Map<String, List<String>>

    fun withMTime(time: DateTime): IPlanetInfo<IDType>
}


data class ServerPlanetInfo(
    override val id: String,
    override val name: String,
    val lastModifiedString: String,
    override val tags: Map<String, List<String>> = emptyMap()
) : IPlanetInfo<String> {

    override val lastModified: DateTime by lazy {
        DateFormat.FORMAT1.parseUtc(lastModifiedString)
    }

    constructor(id: String, name: String, lastModified: DateTime, tags: Map<String, List<String>> = emptyMap()) :
            this(id, name, DateFormat.FORMAT1.format(lastModified), tags)

    companion object {
        fun fromPlanet(id: String, planet: Planet?, lastModified: DateTime, nameOverride: String?=null) =
            ServerPlanetInfo(
                id,
                nameOverride?:planet?.name ?: randomName(),
                lastModified,
                planet?.tagMap.orEmpty()
            )
        fun fromPlanet(id: String, planet: Planet, lastModified: DateTime, nameOverride: String?=null) =
            ServerPlanetInfo(
                id,
                nameOverride?:planet.name,
                lastModified,
                planet.tagMap
            )
    }

    override fun withMTime(time: DateTime): ServerPlanetInfo {
        return copy(lastModifiedString = DateFormat.FORMAT1.format(time))
    }
}
