package de.robolab.common.parser.lines

import de.robolab.common.parser.FileLine
import de.robolab.common.parser.parseCoordinate
import de.robolab.common.parser.serializeCoordinate
import de.robolab.common.planet.Coordinate

class BluePointLine(override val line: String) : FileLine<Coordinate> {

    override val data = parseCoordinate(REGEX.matchEntire(line.trim())!!.groupValues[1])

    override var blockMode: FileLine.BlockMode = FileLine.BlockMode.Unknown

    override fun buildPlanet(builder: FileLine.BuildAccumulator) {
        blockMode = FileLine.BlockMode.Head(builder.previousBlockHead)
        builder.previousBlockHead = this
        builder.planet = builder.planet.copy(
            bluePoint = data
        )
    }

    override fun isAssociatedTo(obj: Any): Boolean {
        if (obj !is Coordinate) return false

        return obj == data
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BluePointLine) return false

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    companion object : FileLine.Parser {
        override val name = "Blue point line parser"
        val REGEX =
            """^blue (-?\d+,-?\d+)\s*(?:#.*?)?$""".toRegex()

        override fun testLine(line: String): Boolean {
            return REGEX.containsMatchIn(line)
        }

        override fun createInstance(line: String): FileLine<*> {
            return BluePointLine(line)
        }

        fun serialize(bluePoint: Coordinate) = buildString {
            append("blue ")
            append(serializeCoordinate(bluePoint))
        }

        fun create(bluePoint: Coordinate) = createInstance(serialize(bluePoint))
    }
}
