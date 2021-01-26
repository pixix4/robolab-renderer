package de.robolab.common.parser.lines

import de.robolab.common.parser.FileLine
import de.robolab.common.planet.PlanetVersion

class VersionLine(override val line: String) : FileLine<PlanetVersion> {

    override val data = REGEX.matchEntire(line.trim())
        ?.groupValues
        ?.getOrNull(2)
        ?.toIntOrNull()
        ?.let {
            PlanetVersion.parse(it)
        } ?: PlanetVersion.FALLBACK

    override var blockMode: FileLine.BlockMode = FileLine.BlockMode.Unknown

    override fun buildPlanet(builder: FileLine.BuildAccumulator) {
        blockMode = FileLine.BlockMode.Head(builder.previousBlockHead)
        builder.previousBlockHead = this
        builder.planet = builder.planet.copy(
            version = data
        )
    }

    override fun isAssociatedTo(obj: Any): Boolean {
        if (obj !is Int) return false

        return obj == data
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VersionLine) return false

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    companion object : FileLine.Parser {
        override val name = "Version line parser"
        val REGEX =
            """^#\s*(VERSION|version)\s?(?::\s*(\d*))?\s*(?:#.*?)?${'$'}""".toRegex()

        override fun testLine(line: String): Boolean {
            return REGEX.containsMatchIn(line)
        }

        override fun createInstance(line: String): FileLine<*> {
            return VersionLine(line)
        }

        fun serialize(version: PlanetVersion) = "# version: ${version.version}"
        fun create(version: PlanetVersion) = createInstance(serialize(version))
    }
}
