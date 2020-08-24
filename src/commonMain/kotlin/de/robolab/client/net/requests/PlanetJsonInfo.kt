package de.robolab.client.net.requests

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.format
import com.soywiz.klock.parseUtc
import de.robolab.client.app.model.file.provider.IFilePlanetIdentifier
import de.robolab.common.net.externalserializers.DateSerializer
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
    val lastModifiedString: String

) : IFilePlanetIdentifier, IPlanetInfo<ID> {

    constructor(id: ID, name: String, lastModified: DateTime) :
            this(id, name, DateFormat.FORMAT1.format(lastModified))

    @Transient
    private var _dateTime : DateTime? = null

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

    fun toPlaintextString(): String = "${id.id}@${DateSerializer.format.format(this.lastModified)}:$name"

    companion object {
        private val textResponseRegex: Regex =
            """^((?:[a-zA-Z0-9_\-]|%3d)+)@([^@]+)@([^\n\r]+)$""".toRegex(RegexOption.MULTILINE)

        fun fromPlaintextString(text: String): PlanetJsonInfo {
            val match = textResponseRegex.matchEntire(text)
                ?: throw IllegalArgumentException("Cannot parse PlanetJsonInfo \"$text\"")
            val (idString: String, modifiedAt: String, name: String) = match.destructured
            return PlanetJsonInfo(
                name = name,
                id = ID(idString),
                lastModifiedString = modifiedAt
            )
        }
    }
}