package de.robolab.common.planet

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.format
import com.soywiz.klock.parseUtc
import de.robolab.common.net.externalserializers.DateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

interface IPlanetInfo<IDType> {
    val id: IDType
    val name: String
    val lastModifiedDate: DateTime

    fun withMTime(time: DateTime): IPlanetInfo<IDType>
}

@Serializable
data class ClientPlanetInfo(
    @Serializable(with = IDSerializer::class)
    override val id: ID,
    override val name: String,
    val lastModified: String
) : IPlanetInfo<ID> {

    constructor(id: ID, name: String, lastModified: DateTime) :
            this(id, name, DateFormat.FORMAT1.format(lastModified))

    override fun withMTime(time: DateTime): ClientPlanetInfo {
        return copy(lastModified = DateFormat.FORMAT1.format(time))
    }

    @Transient
    override val lastModifiedDate: DateTime = DateFormat.FORMAT1.parseUtc(lastModified)

    fun toPlaintextString(): String = "${id.id}@${DateSerializer.format.format(this.lastModifiedDate)}:$name"

    companion object {
        private val textResponseRegex: Regex =
            """^((?:[a-zA-Z0-9_\-]|%3d)+)@([^@]+)@([^\n\r]+)$""".toRegex(RegexOption.MULTILINE)

        fun fromPlaintextString(text: String): ClientPlanetInfo {
            val match = textResponseRegex.matchEntire(text)
                ?: throw IllegalArgumentException("Cannot parse ClientPlanetInfo \"$text\"")
            val (idString: String, modifiedAt: String, name: String) = match.destructured
            return ClientPlanetInfo(
                name = name,
                id = ID(idString),
                lastModified = modifiedAt
            )
        }
    }
}

@Serializable
data class ServerPlanetInfo(
    override val id: String,
    override val name: String,
    val lastModified: String
) : IPlanetInfo<String> {

    constructor(id: String, name: String, lastModified: DateTime) :
            this(id, name, DateFormat.FORMAT1.format(lastModified))

    @Transient
    override val lastModifiedDate: DateTime = DateFormat.FORMAT1.parseUtc(lastModified)

    override fun withMTime(time: DateTime): ServerPlanetInfo {
        return copy(lastModified = DateFormat.FORMAT1.format(time))
    }
}