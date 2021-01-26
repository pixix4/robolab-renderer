package de.robolab.common.parser.lines

import de.robolab.common.parser.*
import de.robolab.common.planet.Path

class PathLine(override val line: String) : FileLine<Path> {

    override val data = REGEX.matchEntire(line.trim())!!.let { match ->
        Path(
            parseCoordinate(match.groupValues[1]),
            parseDirection(match.groupValues[2])!!,
            parseCoordinate(match.groupValues[3]),
            parseDirection(match.groupValues[4])!!,
            match.groupValues[5].toInt(),
            match.groupValues.getOrNull(6)?.split(" ")?.filter { it.isNotBlank() }?.map { parseCoordinate(it) }
                ?.toSet()
                ?: emptySet(),
            emptyList(),
            hidden = false,
            showDirectionArrow = false
        )
    }

    override var blockMode: FileLine.BlockMode = FileLine.BlockMode.Unknown

    override fun buildPlanet(builder: FileLine.BuildAccumulator) {
        blockMode = FileLine.BlockMode.Head(builder.previousBlockHead)
        builder.previousBlockHead = this
        builder.planet = builder.planet.copy(
            pathList = builder.planet.pathList + data
        )
    }

    override fun isAssociatedTo(obj: Any): Boolean {
        if (obj !is Path) return false

        return obj.equalPath(data)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PathLine) return false

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    companion object : FileLine.Parser {
        override val name = "Path line parser"
        val REGEX =
            """^(-?\d+,-?\d+),([NESW]) (-?\d+,-?\d+),([NESW]) (-?\d+)((?: -?\d+,-?\d+)*)( blocked)?\s*(?:#.*?)?$""".toRegex()

        override fun testLine(line: String): Boolean {
            return REGEX.containsMatchIn(line)
        }

        override fun createInstance(line: String): FileLine<*> {
            return PathLine(line)
        }

        fun serialize(path: Path) = buildString {
            append(serializeCoordinate(path.source))
            append(',')
            append(serializeDirection(path.sourceDirection))
            append(' ')
            append(serializeCoordinate(path.target))
            append(',')
            append(serializeDirection(path.targetDirection))
            append(' ')
            append(path.weight ?: 1)
            for (exposure in path.exposure) {
                append(' ')
                append(serializeCoordinate(exposure))
            }
            if (path.weight != null && path.weight < 0) {
                append(" blocked")
            }
        }

        fun create(path: Path) = createInstance(serialize(path))
    }
}
