package de.robolab.common.planet

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.format
import com.soywiz.klock.parseUtc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface IPlanetInfo<IDType> {
    val id: IDType
    val name: String
    val lastModified: DateTime

    fun withMTime(time: DateTime): IPlanetInfo<IDType>
}


@Serializable
data class ServerPlanetInfo(
    override val id: String,
    override val name: String,
    @SerialName("lastModified")
    val lastModifiedString: String
) : IPlanetInfo<String> {

    override val lastModified: DateTime by lazy {
        DateFormat.FORMAT1.parseUtc(lastModifiedString)
    }

    constructor(id: String, name: String, lastModified: DateTime) :
            this(id, name, DateFormat.FORMAT1.format(lastModified))

    override fun withMTime(time: DateTime): ServerPlanetInfo {
        return copy(lastModifiedString = DateFormat.FORMAT1.format(time))
    }
}
