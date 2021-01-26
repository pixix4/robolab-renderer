package de.robolab.common.parser.lines

import de.robolab.common.parser.FileLine
import de.robolab.common.planet.Coordinate

class GroupingLine(override val line: String) : FileLine<Pair<Char, Set<Coordinate>>> {

    override val data = REGEX.matchEntire(line.trim())!!.let { match ->
        match.groupValues[2].first() to match.groupValues[3].split('|').mapNotNull { p ->
            val h = p.split(',').map { it.trim().toInt() }
            if (h.size < 2) null else Coordinate(h[0], h[1])
        }.toSet()
    }

    override var blockMode: FileLine.BlockMode = FileLine.BlockMode.Unknown

    override fun buildPlanet(builder: FileLine.BuildAccumulator) {
        blockMode = FileLine.BlockMode.Head(builder.previousBlockHead)
        builder.previousBlockHead = this
        builder.planet = builder.planet.copy(
            senderGrouping = builder.planet.senderGrouping + (data.second to data.first)
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GroupingLine) return false

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    companion object : FileLine.Parser {
        override val name = "Grouping line parser"
        val REGEX =
            """^#\s*(grouping|GROUPING)\s?(?::\s?([A-Z])\s?)(?::\s?((?:-?\d+,\s*?(?:-?\d+))(?:\s*?\|\s*?(?:-?\d+\s*?,\s*?(?:-?\d+)))*)?)[ \t]*\s*(?:#.*?)?${'$'}""".toRegex()

        override fun testLine(line: String): Boolean {
            return REGEX.containsMatchIn(line)
        }

        override fun createInstance(line: String): FileLine<*> {
            return GroupingLine(line)
        }

        fun serialize(set: Set<Coordinate>, char: Char) = buildString {
            append("# grouping: ")
            append(char)
            append(": ")
            append(set.joinToString(" | ") { (x, y) ->
                "$x,$y"
            })
        }

        fun create(set: Set<Coordinate>, char: Char) = createInstance(serialize(set, char))
    }
}
