package de.robolab.common.parser.lines

import de.robolab.common.parser.*
import de.robolab.common.planet.PathSelect

class PathSelectLine(override val line: String) : FileLine<PathSelect> {

    override val data = REGEX.matchEntire(line.trim())!!.let { match ->
        PathSelect(
            parseCoordinate(match.groupValues[2]),
            parseDirection(match.groupValues[1])!!
        )
    }

    override var blockMode: FileLine.BlockMode = FileLine.BlockMode.Unknown

    override fun buildPlanet(builder: FileLine.BuildAccumulator) {
        blockMode = FileLine.BlockMode.Head(builder.previousBlockHead)
        builder.previousBlockHead = this
        builder.planet = builder.planet.copy(
            pathSelectList = builder.planet.pathSelectList + data
        )
    }

    override fun isAssociatedTo(obj: Any): Boolean {
        if (obj !is PathSelect) return false

        return obj == data
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PathSelectLine) return false

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    companion object : FileLine.Parser {
        override val name = "Path select line parser"
        val REGEX =
            """^direction ([NSWE]) (-?\d+,-?\d+)\s*(?:#.*?)?$""".toRegex()

        override fun testLine(line: String): Boolean {
            return REGEX.containsMatchIn(line)
        }

        override fun createInstance(line: String): FileLine<*> {
            return PathSelectLine(line)
        }

        fun serialize(pathSelect: PathSelect) = buildString {
            append("direction ")
            append(serializeDirection(pathSelect.direction))
            append(' ')
            append(serializeCoordinate(pathSelect.point))
        }

        fun create(pathSelect: PathSelect) = createInstance(serialize(pathSelect))
    }
}
