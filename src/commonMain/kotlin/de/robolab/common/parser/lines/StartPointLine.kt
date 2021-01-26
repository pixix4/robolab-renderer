package de.robolab.common.parser.lines

import de.robolab.common.parser.*
import de.robolab.common.planet.Direction
import de.robolab.common.planet.Path
import de.robolab.common.planet.StartPoint

class StartPointLine(override val line: String) : FileLine<StartPoint> {

    override val data = REGEX.matchEntire(line.trim())!!.let { match ->
        StartPoint(
            parseCoordinate(match.groupValues[1]),
            parseDirection(match.groupValues[3]) ?: Direction.NORTH,
            emptyList()
        )
    }

    override var blockMode: FileLine.BlockMode = FileLine.BlockMode.Unknown

    override fun buildPlanet(builder: FileLine.BuildAccumulator) {
        blockMode = FileLine.BlockMode.Head(builder.previousBlockHead)
        builder.previousBlockHead = this
        builder.planet = builder.planet.copy(
            startPoint = data
        )
    }

    override fun isAssociatedTo(obj: Any): Boolean {
        if (obj is Path) {
            if (obj.equalPath(data.path)) {
                return true
            }
        }
        if (obj !is StartPoint) return false

        return obj.equalPoint(data)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StartPointLine) return false

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    companion object : FileLine.Parser {
        override val name = "Start point line parser"
        val REGEX =
            """^start (-?\d+,-?\d+)(,([NESW]))?\s*(?:#.*?)?$""".toRegex()

        override fun testLine(line: String): Boolean {
            return REGEX.containsMatchIn(line)
        }

        override fun createInstance(line: String): FileLine<*> {
            return StartPointLine(line)
        }

        fun serialize(startPoint: StartPoint) = buildString {
            append("start ")
            append(serializeCoordinate(startPoint.point))
            if (startPoint.orientation != Direction.NORTH) {
                append(',')
                append(serializeDirection(startPoint.orientation))
            }
        }

        fun create(startPoint: StartPoint) = createInstance(serialize(startPoint))
    }
}
