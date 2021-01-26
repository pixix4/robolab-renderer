package de.robolab.common.parser.lines

import de.robolab.common.parser.FileLine
import de.robolab.common.parser.parseCoordinate
import de.robolab.common.parser.serializeCoordinate
import de.robolab.common.planet.TargetPoint

class TargetLine(override val line: String) : FileLine<TargetPoint> {

    override val data = REGEX.matchEntire(line.trim())!!.let { match ->
        TargetPoint(
            parseCoordinate(match.groupValues[1]),
            parseCoordinate(match.groupValues[2])
        )
    }

    override var blockMode: FileLine.BlockMode = FileLine.BlockMode.Unknown

    override fun buildPlanet(builder: FileLine.BuildAccumulator) {
        blockMode = FileLine.BlockMode.Head(builder.previousBlockHead)
        builder.previousBlockHead = this
        builder.planet = builder.planet.copy(
            targetList = builder.planet.targetList + data
        )
    }

    override fun isAssociatedTo(obj: Any): Boolean {
        if (obj !is TargetPoint) return false

        return obj == data
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TargetLine) return false

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    companion object : FileLine.Parser {
        override val name = "Target line parser"
        val REGEX =
            """^target (-?\d+,-?\d+) (-?\d+,-?\d+)\s*(?:#.*?)?$""".toRegex()

        override fun testLine(line: String): Boolean {
            return REGEX.containsMatchIn(line)
        }

        override fun createInstance(line: String): FileLine<*> {
            return TargetLine(line)
        }

        fun serialize(target: TargetPoint) = buildString {
            append("target ")
            append(serializeCoordinate(target.target))
            append(' ')
            append(serializeCoordinate(target.exposure))
        }

        fun create(target: TargetPoint) = createInstance(serialize(target))
    }
}
