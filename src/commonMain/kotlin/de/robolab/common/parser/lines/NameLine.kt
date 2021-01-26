package de.robolab.common.parser.lines

import de.robolab.common.parser.FileLine

class NameLine(override val line: String) : FileLine<String> {

    override val data = REGEX.matchEntire(line.trim())?.groupValues?.getOrNull(2) ?: ""

    override var blockMode: FileLine.BlockMode = FileLine.BlockMode.Unknown

    override fun buildPlanet(builder: FileLine.BuildAccumulator) {
        blockMode = FileLine.BlockMode.Head(builder.previousBlockHead)
        builder.previousBlockHead = this
        builder.planet = builder.planet.copy(
            name = data
        )
    }

    override fun isAssociatedTo(obj: Any): Boolean {
        if (obj !is String) return false

        return obj == data
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NameLine) return false

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    companion object : FileLine.Parser {
        override val name = "Name line parser"
        val REGEX =
            """^#\s*(NAME|name)\s?(?::\s*(\w[^\n]*?))?\s*(?:#.*?)?$""".toRegex()

        override fun testLine(line: String): Boolean {
            return REGEX.containsMatchIn(line)
        }

        override fun createInstance(line: String): FileLine<*> {
            return NameLine(line)
        }

        fun serialize(name: String) = "# name: $name"
        fun create(name: String) = createInstance(serialize(name))
    }
}
