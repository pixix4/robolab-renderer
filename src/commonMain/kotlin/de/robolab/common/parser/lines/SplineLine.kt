package de.robolab.common.parser.lines

import de.robolab.common.parser.FileLine
import de.robolab.common.parser.toFixed
import de.robolab.common.planet.Path
import de.robolab.common.utils.Point

class SplineLine(override val line: String) : FileLine<List<Point>> {

    override val data = REGEX.matchEntire(line.trim())!!.let { match ->
        match.groupValues[3].split('|').mapNotNull { p ->
            val h = p.split(',').mapNotNull { it.trim().toDoubleOrNull() }
            if (h.size < 2) null else Point(h[0], h[1])
        }
    }

    var associatedPath: Path? = null

    override var blockMode: FileLine.BlockMode = FileLine.BlockMode.Unknown

    override fun buildPlanet(builder: FileLine.BuildAccumulator) {
        val previousBlockHead = builder.previousBlockHead
            ?: throw IllegalArgumentException("Spline line: previous block is null")
        blockMode = FileLine.BlockMode.Append(previousBlockHead)

        if (previousBlockHead is PathLine) {
            val path = builder.planet.pathList.last().copy(
                controlPoints = data
            )
            associatedPath = path
            builder.planet = builder.planet.copy(
                pathList = builder.planet.pathList.dropLast(1) + path
            )
            return
        }
        if (previousBlockHead is StartPointLine) {
            val startPoint = builder.planet.startPoint
                ?: throw IllegalArgumentException("Spline line: start point is null")
            associatedPath = previousBlockHead.data.path
            builder.planet = builder.planet.copy(
                startPoint = startPoint.copy(
                    controlPoints = data
                )
            )
            return
        }

        throw IllegalArgumentException("Spline line: previous block is ${previousBlockHead::class.simpleName}")
    }

    override fun isAssociatedTo(obj: Any): Boolean {
        if (obj is List<*>) {
            return obj == data
        }

        return super.isAssociatedTo(obj)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SplineLine) return false

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    companion object : FileLine.Parser {
        override val name = "Spline line parser"
        val REGEX =
            """^#\s*(SPLINE|spline)\s?(?:\(\s*((?:[^\s,][^\n,]*?)?(?:\s*?,\s*?[^\s,][^\n,]*?)*)\s*\))?(?::\s?((?:-?\d+(?:\.\d+)?\s*?,\s*?(?:-?\d+(?:\.\d+)?))(?:\s*?\|\s*?(?:-?\d+(?:\.\d+)?\s*?,\s*?(?:-?\d+(?:\.\d+)?)))*)?)[ \t]*\s*(?:#.*?)?$""".toRegex()

        override fun testLine(line: String): Boolean {
            return REGEX.containsMatchIn(line)
        }

        override fun createInstance(line: String): FileLine<*> {
            return SplineLine(line)
        }

        fun serialize(spline: List<Point>) = buildString {
            append("# spline: ")
            append(spline.joinToString(" | ") {
                val x = it.left.toFixed(2)
                val y = it.top.toFixed(2)
                "$x,$y"
            })
        }

        fun create(spline: List<Point>) = createInstance(serialize(spline))
    }
}
