package de.robolab.common.planet

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.format
import de.robolab.common.net.externalserializers.DateSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

interface IPlanetInfo<IDType> {
    val id: IDType
    val name: String
    val lastModified: DateTime

    fun withMTime(time: DateTime): IPlanetInfo<IDType>
}

@Serializable
data class ClientPlanetInfo(
    @Serializable(with = IDSerializer::class)
    override val id: ID,
    override val name: String,
    @Serializable(with = DateSerializer::class)
    override val lastModified: DateTime
) : IPlanetInfo<ID> {
    override fun withMTime(time: DateTime): ClientPlanetInfo {
        return copy(lastModified = time)
    }

    fun toPlaintextString(): String = "${id.id}@${DateSerializer.format.format(lastModified)}:$name"

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
                lastModified = DateTime.fromString(modifiedAt).utc
            )
        }
    }
}

@Serializable
data class ServerPlanetInfo(
    override val id: String,
    override val name: String,
    @Serializable(with = DateSerializer::class)
    override val lastModified: DateTime
) : IPlanetInfo<String> {
    override fun withMTime(time: DateTime): ServerPlanetInfo {
        return copy(lastModified = time)
    }
}